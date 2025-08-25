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
import ru.yandex.practicum.jpa_entities.Scenario;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.repositories.ScenarioRepository;

import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class SnapshotProcessor implements Runnable {
    private final ScenarioRepository scenarioRepository;
    private final ScenarioChecker scenarioChecker;
    private final Deserializer<SensorsSnapshotAvro> snapshotDeserializer;

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.topics.snapshots}")
    private String snapshotsTopic;

    @Value("${kafka.consumer.group-id}")
    private String groupId;

    private KafkaConsumer<String, SensorsSnapshotAvro> consumer;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread thread;

    @Override
    public void run() {
        running.set(true);
        try {
            Properties props = new Properties();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId + "-snapshot");
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, snapshotDeserializer.getClass().getName());
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

            consumer = new KafkaConsumer<>(props);
            consumer.subscribe(Collections.singletonList(snapshotsTopic));

            log.info("SnapshotProcessor started for topic: {}", snapshotsTopic);

            while (running.get()) {
                ConsumerRecords<String, SensorsSnapshotAvro> records = consumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<String, SensorsSnapshotAvro> record : records) {
                    try {
                        processSnapshot(record.value());
                        consumer.commitSync();
                    } catch (Exception e) {
                        log.error("Error processing snapshot", e);
                    }
                }
            }
        } catch (WakeupException e) {
            // Ignore for shutdown
        } catch (Exception e) {
            log.error("Error in SnapshotProcessor", e);
        } finally {
            if (consumer != null) {
                consumer.close();
            }
            running.set(false);
            log.info("SnapshotProcessor stopped");
        }
    }

    private void processSnapshot(SensorsSnapshotAvro snapshot) {
        String hubId = snapshot.getHubId().toString();

        // Эффективная загрузка сценариев со всеми связями
        List<Scenario> scenarios = scenarioRepository.findByHubIdWithConditionsAndSensors(hubId);

        if (scenarios.isEmpty()) {
            log.debug("No scenarios found for hub: {}", hubId);
            return;
        }

        // Check each scenario
        for (Scenario scenario : scenarios) {
            boolean conditionsMet = scenarioChecker.checkConditions(scenario, snapshot);

            if (conditionsMet) {
                scenarioChecker.executeActions(scenario, hubId);
                log.info("Scenario executed: {} for hub: {}", scenario.getName(), hubId);
            }
        }
    }

    public void start() {
        if (thread == null || !thread.isAlive()) {
            thread = new Thread(this, "SnapshotProcessor");
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
