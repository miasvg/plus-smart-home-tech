package ru.practicum.aggregation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class SnapshotAggregator {
    private final Map<String, SensorsSnapshotAvro> snapshots = new ConcurrentHashMap<>();

    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        String hubId = event.getHubId();
        String sensorId = event.getId();
        Instant eventTimestamp = event.getTimestamp();

        // Получаем или создаем снапшот для хаба
        SensorsSnapshotAvro snapshot = snapshots.computeIfAbsent(hubId, id ->
                SensorsSnapshotAvro.newBuilder()
                        .setHubId(hubId)
                        .setTimestamp(eventTimestamp)
                        .setSensorsState(new HashMap<>())
                        .build()
        );

        // Проверяем текущее состояние датчика
        SensorStateAvro currentState = snapshot.getSensorsState().get(sensorId);

        // Если состояние уже есть и оно новее или такое же - пропускаем
        if (currentState != null) {
            if (currentState.getTimestamp().isAfter(eventTimestamp)) {
                log.debug("Событие устарело для датчика {}", sensorId);
                return Optional.empty();
            }

            if (currentState.getTimestamp() == eventTimestamp &&
                    Objects.equals(currentState.getData(), event.getPayload())) {
                log.debug("Дублирующее событие для датчика {}", sensorId);
                return Optional.empty();
            }
        }

        // Создаем новое состояние датчика
        SensorStateAvro newState = SensorStateAvro.newBuilder()
                .setTimestamp(eventTimestamp)
                .setData(event.getPayload())
                .build();

        // Обновляем снапшот
        Map<String, SensorStateAvro> newStateMap = new HashMap<>(snapshot.getSensorsState());
        newStateMap.put(sensorId, newState);

        SensorsSnapshotAvro updatedSnapshot = SensorsSnapshotAvro.newBuilder(snapshot)
                .setTimestamp(eventTimestamp)
                .setSensorsState(newStateMap)
                .build();

        snapshots.put(hubId, updatedSnapshot);

        log.info("Обновлен снапшот для хаба {}, датчик {}", hubId, sensorId);
        return Optional.of(updatedSnapshot);
    }

    public Optional<SensorsSnapshotAvro> getSnapshot(String hubId) {
        return Optional.ofNullable(snapshots.get(hubId));
    }

    public void clearSnapshot(String hubId) {
        snapshots.remove(hubId);
    }
}