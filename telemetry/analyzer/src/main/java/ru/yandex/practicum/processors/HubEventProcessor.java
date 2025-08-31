package ru.yandex.practicum.processors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.config.KafkaConfig;
import ru.yandex.practicum.deserializers.HubEventDeserializerAnalyzer;
import ru.yandex.practicum.deserializers.SensorsSnapshotDeserializer;
import ru.yandex.practicum.dto.ActionType;
import ru.yandex.practicum.dto.ConditionOperation;
import ru.yandex.practicum.dto.ConditionType;
import ru.yandex.practicum.handlers.HubEventHandler;
import ru.yandex.practicum.handlers.HubHandler;
import ru.yandex.practicum.jpa_entities.*;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.repositories.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubEventProcessor implements Runnable {

    private final KafkaConfig kafkaProperties;
    private Consumer<String, HubEventAvro> consumer;
    private final HubHandler hubHandler;

    @Value("${kafka.topics.hubs}")
    private String topic;

    @PostConstruct
    public void init() {
        // собрали consumer из пропертей
        this.consumer = new KafkaConsumer<>(
                kafkaProperties.hubEventConsumerFactory().getConfigurationProperties(),
                new StringDeserializer(),
                new HubEventDeserializerAnalyzer()
        );
    }
    @Override
    public void run() {
        try {
            consumer.subscribe(List.of(topic));
            Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
            Map<String, HubEventHandler> mapBuilder = hubHandler.getHandlers();

            while (true) {
                ConsumerRecords<String, HubEventAvro> records = consumer.poll(Duration.ofMillis(1000));

                for (ConsumerRecord<String, HubEventAvro> record : records) {
                    HubEventAvro event = record.value();
                    String payloadName = event.getPayload().getClass().getSimpleName();
                    log.info("Получение хаба {}", payloadName);
                    if (mapBuilder.containsKey(payloadName)) {
                        mapBuilder.get(payloadName).handle(event);
                    } else {
                        throw new IllegalArgumentException("Нет обработчика для события " + event);
                    }
                }
                consumer.commitSync();
            }
        } catch (WakeupException ignored) {
        } catch (Exception e) {
            log.error("Ошибка получения данных {}", topic);
        } finally {
            try {
                consumer.commitSync();
            } finally {
                consumer.close();
            }
        }
    }
}
