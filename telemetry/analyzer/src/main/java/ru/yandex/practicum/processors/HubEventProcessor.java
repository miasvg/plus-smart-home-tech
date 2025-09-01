package ru.yandex.practicum.processors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.deserializers.HubEventDeserializerAnalyzer;
import ru.yandex.practicum.handlers.HubEventHandler;
import ru.yandex.practicum.handlers.HubHandler;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubEventProcessor implements Runnable {

    private final HubHandler hubHandler;
    private Consumer<String, HubEventAvro> consumer;

    @Value("${kafka.topics.hubs}")
    private String topic;

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @PostConstruct
    public void init() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "hub-event-processor-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, HubEventDeserializerAnalyzer.class.getName());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");

        consumer = new KafkaConsumer<>(props, new StringDeserializer(), new HubEventDeserializerAnalyzer());
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
                        log.warn("Нет обработчика для события {}", event);
                    }
                }
                consumer.commitSync();
            }
        } catch (WakeupException ignored) {
        } catch (Exception e) {
            log.error("Ошибка получения данных {}", topic, e);
        } finally {
            try {
                consumer.commitSync();
            } finally {
                consumer.close();
            }
        }
    }
}

