package ru.yandex.practicum.config;

import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.deserializers.HubEventDeserializerAnalyzer;
import ru.yandex.practicum.deserializers.SensorsSnapshotDeserializer;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

@Configuration
public class KafkaConfig {

    @Bean
    public Deserializer<HubEventAvro> hubEventDeserializer() {
        return new HubEventDeserializerAnalyzer();
    }

    @Bean
    public Deserializer<SensorsSnapshotAvro> snapshotDeserializer() {
        return new SensorsSnapshotDeserializer();
    }
}
