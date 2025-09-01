package ru.yandex.practicum.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import ru.yandex.practicum.deserializers.HubEventDeserializerAnalyzer;
import ru.yandex.practicum.deserializers.SensorsSnapshotDeserializer;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
@RequiredArgsConstructor

public class KafkaConfig {
    private final Environment environment;

    @Bean
    public Deserializer<HubEventAvro> hubEventDeserializer() {
        return new HubEventDeserializerAnalyzer();
    }

    @Bean
    public Deserializer<SensorsSnapshotAvro> snapshotDeserializer() {
        return new SensorsSnapshotDeserializer();
    }
  /*  @Bean
    public ConsumerFactory<String, HubEventAvro> hubEventConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, environment.getProperty("spring.kafka.consumer.client-id"));
        props.put(ConsumerConfig.GROUP_ID_CONFIG, environment.getProperty("spring.kafka.consumer.group-id"));
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, environment.getProperty("spring.kafka.bootstrap-servers"));
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, environment.getProperty("spring.kafka.consumer.key-deserializer"));
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, HubEventDeserializerAnalyzer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, environment.getProperty("spring.kafka.consumer.enable-auto-commit"));
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), hubEventDeserializer());
    }

    @Bean
    public ConsumerFactory<String, SensorsSnapshotAvro> snapshotConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, environment.getProperty("spring.kafka.consumer.snapshots-client-id"));
        props.put(ConsumerConfig.GROUP_ID_CONFIG, environment.getProperty("spring.kafka.consumer.snapshots-group-id"));
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, environment.getProperty("spring.kafka.bootstrap-servers"));
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, environment.getProperty("spring.kafka.consumer.key-deserializer"));
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SensorsSnapshotDeserializer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, environment.getProperty("spring.kafka.consumer.enable-auto-commit"));
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), snapshotDeserializer());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, HubEventAvro> hubEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, HubEventAvro> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(hubEventConsumerFactory());
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SensorsSnapshotAvro> snapshotKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, SensorsSnapshotAvro> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(snapshotConsumerFactory());
        return factory;
    }
    */
}

