package ru.yandex.practicum.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.repositories.SensorRepository;

@Component
@RequiredArgsConstructor
public class DeviceRemovedHandler implements HubEventHandler {

    private final SensorRepository sensorRepository;

    @Override
    @Transactional
    public void handle(HubEventAvro hubEvent) {
        DeviceRemovedEventAvro deviceRemovedEvent = (DeviceRemovedEventAvro) hubEvent.getPayload();
        sensorRepository.deleteByIdAndHubId(deviceRemovedEvent.getId(), hubEvent.getHubId());
    }

    @Override
    public String getMessageType() {
        return DeviceRemovedEventAvro.class.getSimpleName();
    }
}
