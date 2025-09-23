package ru.yandex.practicum.kafka;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

@Component
public class SensorsSnapshotSerializer extends BaseAvroSerializer<SensorsSnapshotAvro> {
}

