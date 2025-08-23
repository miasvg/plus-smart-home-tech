package ru.practicum.service;

import ru.practicum.dto.hubs.HubEvent;
import ru.practicum.dto.sensors.SensorEvent;
import lombok.RequiredArgsConstructor;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.mapper.HubAndSensorMapper;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


@Service
@RequiredArgsConstructor
@Slf4j
public class TelemetryService {
    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private final HubAndSensorMapper mapper;

    @Value("${kafka.topics.sensors}")
    private String sensorsTopic;

    @Value("${kafka.topics.hubs}")
    private String hubsTopic;

    public void send(SensorEventAvro event) {
        try {
            byte[] avroData = serializeToAvro(event);
            sendWithCallback(sensorsTopic, event.getHubId(), avroData, "датчика");
        } catch (IOException e) {
            log.error("Ошибка сериализации события датчика", e);
        }
    }

    public void send(HubEventAvro event) {
        try {
            byte[] avroData = serializeToAvro(event);
            sendWithCallback(hubsTopic, event.getHubId(), avroData, "хаба");
        } catch (IOException e) {
            log.error("Ошибка сериализации события хаба", e);
        }
    }

    private <T extends SpecificRecord> byte[] serializeToAvro(SpecificRecord record) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);

        try {
            DatumWriter<SpecificRecord> datumWriter = new SpecificDatumWriter<>(record.getSchema());
            datumWriter.write(record, encoder);
            encoder.flush();
            return out.toByteArray();
        } finally {
            out.close();
        }
    }

    private void sendWithCallback(String topic, String key, byte[] data, String eventType) {
        kafkaTemplate.send(topic, key, data)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Ошибка отправки события {} в топик {}", eventType, topic, ex);
                    } else {
                        log.info("Событие {} успешно отправлено в топик {} (offset: {})",
                                eventType, topic, result.getRecordMetadata().offset());
                    }
                });
    }
}