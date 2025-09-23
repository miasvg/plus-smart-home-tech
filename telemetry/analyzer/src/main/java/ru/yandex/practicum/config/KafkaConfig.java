package ru.yandex.practicum.config;


import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.kafka.HubEventDeserializerAnalyzer;
import ru.yandex.practicum.kafka.SensorsSnapshotDeserializer;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

@Configuration
@RequiredArgsConstructor

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

