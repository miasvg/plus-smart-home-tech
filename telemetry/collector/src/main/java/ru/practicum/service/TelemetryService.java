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
import java.io.ByteArrayOutputStream;
import java.io.IOException;


@Service
@RequiredArgsConstructor
@Slf4j
public class TelemetryService {
    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private final HubAndSensorMapper mapper;
    private final DatumWriter<SpecificRecord> datumWriter = new SpecificDatumWriter<>();

    @Value("${kafka.topics.sensors}")
    private String sensorsTopic;

    @Value("${kafka.topics.hubs}")
    private String hubsTopic;

    public void send(SensorEvent event) {
        try {
            byte[] avroData = serializeToAvro(mapper.toAvro(event));
            sendWithCallback(sensorsTopic, event.getHubId(), avroData, "датчика");
        } catch (IOException e) {
            log.error("Ошибка сериализации события датчика", e);
        }
    }

    public void send(HubEvent event) {
        try {
            byte[] avroData = serializeToAvro(mapper.toAvro(event));
            sendWithCallback(hubsTopic, event.getHubId(), avroData, "хаба");
        } catch (IOException e) {
            log.error("Ошибка сериализации события хаба", e);
        }
    }

    private byte[] serializeToAvro(SpecificRecord record) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);

        try {
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