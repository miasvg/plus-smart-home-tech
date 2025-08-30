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
        if (event == null || event.getHubId() == null || event.getId() == null) {
            log.warn("Skip event: nulls in hubId/id");
            return Optional.empty();
        }

        final String hubId = event.getHubId().toString();
        final String sensorId = event.getId().toString();
        final Instant ts = event.getTimestamp();

        // Берём текущее состояние хаба (или создаём пустое)
        SensorsSnapshotAvro current = snapshots.get(hubId);
        if (current == null) {
            current = SensorsSnapshotAvro.newBuilder()
                    .setHubId(hubId)
                    .setTimestamp(ts)
                    .setSensorsState(new HashMap<>())
                    .build();
        }

        final Map<String, SensorStateAvro> stateMap = current.getSensorsState();
        final SensorStateAvro existing = stateMap.get(sensorId);

        // Если у нас уже есть более "свежее" значение по этому сенсору — игнорируем
        if (existing != null) {
            Instant existedTs = existing.getTimestamp();
            if (existedTs != null && existedTs.isAfter(ts)) {
                log.debug("Skip outdated event: hub={}, sensor={}, existedTs={}, eventTs={}",
                        hubId, sensorId, existedTs, ts);
                return Optional.empty();
            }
            // Если время одинаковое и данные идентичны — это дубликат
            if (existedTs != null && existedTs.equals(ts)
                    && Objects.equals(existing.getData(), event.getPayload())) {
                log.debug("Skip duplicate event: hub={}, sensor={}, ts={}", hubId, sensorId, ts);
                return Optional.empty();
            }
        }

        // Обновляем конкретный сенсор
        SensorStateAvro newState = SensorStateAvro.newBuilder()
                .setTimestamp(ts)
                .setData(event.getPayload())
                .build();

        Map<String, SensorStateAvro> newMap = new HashMap<>(stateMap);
        newMap.put(sensorId, newState);

        // timestamp снапшота — это "последнее обновление по хабу"
        Instant snapshotTs = current.getTimestamp();
        if (snapshotTs == null || ts.isAfter(snapshotTs)) {
            snapshotTs = ts;
        }

        SensorsSnapshotAvro updated = SensorsSnapshotAvro.newBuilder(current)
                .setHubId(hubId)                 // жёстко из события
                .setTimestamp(snapshotTs)        // не регрессим
                .setSensorsState(newMap)
                .build();

        snapshots.put(hubId, updated);

        log.info("Snapshot updated: hub={}, sensor={}, ts={}", hubId, sensorId, ts);
        return Optional.of(updated);
    }


    public Optional<SensorsSnapshotAvro> getSnapshot(String hubId) {
        return Optional.ofNullable(snapshots.get(hubId));
    }

    public void clearSnapshot(String hubId) {
        snapshots.remove(hubId);
    }
}