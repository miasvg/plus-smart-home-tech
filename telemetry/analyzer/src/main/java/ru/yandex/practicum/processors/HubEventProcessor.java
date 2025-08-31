package ru.yandex.practicum.processors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.jpa_entities.*;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.repositories.*;

import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubEventProcessor implements Runnable {
    private final SensorRepository sensorRepository;
    private final ScenarioRepository scenarioRepository;
    private final ConditionRepository conditionRepository;
    private final ActionRepository actionRepository;
    private final ScenarioConditionRepository scenarioConditionRepository;
    private final ScenarioActionRepository scenarioActionRepository;
    private final Deserializer<HubEventAvro> hubEventDeserializer;

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.topics.hubs}")
    private String hubsTopic;

    @Value("${kafka.consumer.group-id}")
    private String groupId;

    private KafkaConsumer<String, HubEventAvro> consumer;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread thread;

    @Override
    public void run() {
        running.set(true);
        try {
            Properties props = new Properties();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId + "-hub");
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, hubEventDeserializer.getClass().getName());
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

            consumer = new KafkaConsumer<>(props);
            consumer.subscribe(Collections.singletonList(hubsTopic));

            log.info("HubEventProcessor started for topic: {}", hubsTopic);

            while (running.get()) {
                ConsumerRecords<String, HubEventAvro> records = consumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<String, HubEventAvro> record : records) {
                    try {
                        processHubEvent(record.value());
                        consumer.commitSync();
                    } catch (Exception e) {
                        log.error("Error processing hub event", e);
                    }
                }
            }
        } catch (WakeupException e) {
            // Ignore for shutdown
        } catch (Exception e) {
            log.error("Error in HubEventProcessor", e);
        } finally {
            if (consumer != null) {
                consumer.close();
            }
            running.set(false);
            log.info("HubEventProcessor stopped");
        }
    }

    private void processHubEvent(HubEventAvro event) {
        String hubId = event.getHubId();

        switch (event.getPayload().getClass().getSimpleName()) {
            case "DeviceAddedEventAvro":
                processDeviceAdded(event, hubId);
                break;
            case "DeviceRemovedEventAvro":
                processDeviceRemoved(event, hubId);
                break;
            case "ScenarioAddedEventAvro":
                processScenarioAdded(event, hubId);
                break;
            case "ScenarioRemovedEventAvro":
                processScenarioRemoved(event, hubId);
                break;
            default:
                log.warn("Unknown hub event type: {}", event.getPayload().getClass().getSimpleName());
        }
    }

    private void processDeviceAdded(HubEventAvro event, String hubId) {
        var payload = (ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro) event.getPayload();

        Sensor sensor = new Sensor(payload.getId(), hubId);
        sensorRepository.save(sensor);
        log.info("Device added: {} for hub: {}", payload.getId(), hubId);
    }

    private void processDeviceRemoved(HubEventAvro event, String hubId) {
        var payload = (ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro) event.getPayload();

        sensorRepository.deleteById(payload.getId());
        log.info("Device removed: {} from hub: {}", payload.getId(), hubId);
    }

    private void processScenarioAdded(HubEventAvro event, String hubId) {
        var payload = (ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro) event.getPayload();

        // Если сценарий с таким именем уже есть — удалим
        scenarioRepository.findByHubIdAndName(hubId, payload.getName())
                .ifPresent(existing -> {
                    // удаляем все связанные условия и действия
                    scenarioConditionRepository.deleteAll(existing.getConditions());
                    scenarioActionRepository.deleteAll(existing.getActions());
                    scenarioRepository.delete(existing);
                });

        // 1. Создаём и сохраняем новый сценарий
        Scenario scenario = new Scenario();
        scenario.setHubId(hubId);
        scenario.setName(payload.getName());
        scenario = scenarioRepository.save(scenario); // ID присвоен

        // 2. Сохраняем условия
        for (var conditionAvro : payload.getConditions()) {
            // Сохраняем Condition отдельно
            Condition condition = new Condition();
            condition.setType(conditionAvro.getType());
            condition.setOperation(conditionAvro.getOperation());
            if (conditionAvro.getValue() instanceof Integer) {
                condition.setValue((Integer) conditionAvro.getValue());
            }
            condition = conditionRepository.save(condition);

            // Получаем или создаём Sensor
            Sensor sensor = sensorRepository.findById(conditionAvro.getSensorId())
                    .orElseGet(() -> sensorRepository.save(new Sensor(conditionAvro.getSensorId(), hubId)));

            // Создаём ScenarioCondition и сохраняем
            ScenarioCondition sc = new ScenarioCondition();
            sc.setScenario(scenario);
            sc.setCondition(condition);
            sc.setSensor(sensor);

            scenarioConditionRepository.save(sc);
        }

        // 3. Сохраняем действия
        for (var actionAvro : payload.getActions()) {
            // Сохраняем Action отдельно
            Action action = new Action();
            action.setType(actionAvro.getType());
            action.setValue(actionAvro.getValue());
            action = actionRepository.save(action);

            // Получаем или создаём Sensor
            Sensor sensor = sensorRepository.findById(actionAvro.getSensorId())
                    .orElseGet(() -> sensorRepository.save(new Sensor(actionAvro.getSensorId(), hubId)));

            // Формируем составной ключ для ScenarioAction
            ScenarioActionId saId = new ScenarioActionId();
            saId.setScenarioId(scenario.getId());
            saId.setActionId(action.getId());
            saId.setSensorId(sensor.getId());

            ScenarioAction sa = new ScenarioAction();
            sa.setId(saId);
            sa.setScenario(scenario);
            sa.setAction(action);
            sa.setSensor(sensor);

            scenarioActionRepository.save(sa);
        }

        log.info("Scenario added: {} for hub: {} with {} conditions and {} actions",
                payload.getName(), hubId, payload.getConditions().size(), payload.getActions().size());
    }





    private void processScenarioRemoved(HubEventAvro event, String hubId) {
        var payload = (ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro) event.getPayload();

        scenarioRepository.findByHubIdAndName(hubId, payload.getName().toString())
                .ifPresent(scenarioRepository::delete);

        log.info("Scenario removed: {} from hub: {}", payload.getName(), hubId);
    }

    public void start() {
        if (thread == null || !thread.isAlive()) {
            thread = new Thread(this, "HubEventProcessor");
            thread.start();
        }
    }

    @PreDestroy
    public void shutdown() {
        running.set(false);
        if (consumer != null) {
            consumer.wakeup();
        }
        if (thread != null) {
            try {
                thread.join(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
