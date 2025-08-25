package ru.practicum.aggregation;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.deserialisers.SensorEventDeserializer;
import ru.practicum.deserialisers.SensorsSnapshotSerializer;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

@Component
@RequiredArgsConstructor
@Slf4j
public class AggregationStarter {
    private final SnapshotAggregator snapshotAggregator;

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.topics.sensors}")
    private String sensorsTopic;

    @Value("${kafka.topics.snapshots}")
    private String snapshotsTopic;

    public void start() {
        KafkaConsumer<String, SensorEventAvro> consumer = null;
        KafkaProducer<String, SensorsSnapshotAvro> producer = null;

        try {
            // Настройка consumer
            Properties consumerProps = new Properties();
            consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "aggregator-group");
            consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SensorEventDeserializer.class);
            consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

            consumer = new KafkaConsumer<>(consumerProps);
            consumer.subscribe(Collections.singletonList(sensorsTopic));

            // Настройка producer
            Properties producerProps = new Properties();
            producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, SensorsSnapshotSerializer.class);

            producer = new KafkaProducer<>(producerProps);

            log.info("Запуск агрегации событий...");

            while (true) {
                ConsumerRecords<String, SensorEventAvro> records = consumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<String, SensorEventAvro> record : records) {
                    try {
                        SensorEventAvro event = record.value();
                        Optional<SensorsSnapshotAvro> updatedSnapshot = snapshotAggregator.updateState(event);

                        if (updatedSnapshot.isPresent()) {
                            SensorsSnapshotAvro snapshot = updatedSnapshot.get();
                            ProducerRecord<String, SensorsSnapshotAvro> snapshotRecord =
                                    new ProducerRecord<>(snapshotsTopic, snapshot.getHubId(), snapshot);

                            producer.send(snapshotRecord, (metadata, exception) -> {
                                if (exception != null) {
                                    log.error("Ошибка отправки снапшота в Kafka", exception);
                                } else {
                                    log.debug("Снапшот отправлен в топик {}, offset: {}",
                                            metadata.topic(), metadata.offset());
                                }
                            });
                        }
                    } catch (Exception e) {
                        log.error("Ошибка обработки события", e);
                    }
                }

                consumer.commitSync();
            }

        } catch (WakeupException ignored) {
            // Игнорируем для корректного завершения
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий", e);
        } finally {
            try {
                if (producer != null) {
                    producer.flush();
                    producer.close(Duration.ofSeconds(5));
                }
            } finally {
                if (consumer != null) {
                    consumer.close();
                }
            }
            log.info("Агрегация остановлена");
        }
    }

    @PreDestroy
    public void shutdown() {
        // Метод для корректного завершения
    }
}
