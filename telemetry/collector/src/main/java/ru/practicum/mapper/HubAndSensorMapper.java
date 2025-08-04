package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.hubs.*;
import ru.practicum.dto.sensors.*;
import ru.yandex.practicum.kafka.telemetry.event.*;
import java.time.Instant;
import java.util.stream.Collectors;

@Component
public class HubAndSensorMapper {
    public SensorEventAvro toAvro(SensorEvent dto) {
        SensorEventAvro.Builder builder = SensorEventAvro.newBuilder()
                .setId(dto.getId())
                .setHubId(dto.getHubId())
                .setTimestamp(dto.getTimestamp());

        // Используем switch-expression для лаконичного определения payload
        Object payload = switch (dto.getType()) {
            case CLIMATE_SENSOR_EVENT -> toAvro((ClimateSensorEvent) dto);
            case LIGHT_SENSOR_EVENT -> toAvro((LightSensorEvent) dto);
            case MOTION_SENSOR_EVENT -> toAvro((MotionSensorEvent) dto);
            case SWITCH_SENSOR_EVENT -> toAvro((SwitchSensorEvent) dto);
            case TEMPERATURE_SENSOR_EVENT -> toAvro((TemperatureSensorEvent) dto);
        };
        builder.setPayload(payload);
        return builder.build();
    }

    public HubEventAvro toAvro(HubEvent dto) {
        HubEventAvro.Builder builder = HubEventAvro.newBuilder()
                .setHubId(dto.getHubId())
                .setTimestamp(Instant.ofEpochSecond(dto.getTimestamp().toEpochMilli()));

        Object payload = switch (dto.getType()) {
            case DEVICE_ADDED -> toAvro((DeviceAddedEvent) dto);
            case DEVICE_REMOVED -> toAvro((DeviceRemovedEvent) dto);
            case SCENARIO_ADDED -> toAvro((ScenarioAddedEvent) dto);
            case SCENARIO_REMOVED -> toAvro((ScenarioRemovedEvent) dto);
        };
        builder.setPayload(payload);
        return builder.build();
    }


    public ClimateSensorAvro toAvro(ClimateSensorEvent dto) {
        return ClimateSensorAvro.newBuilder()
                .setTemperatureC(dto.getTemperatureC())
                .setHumidity(dto.getHumidity())
                .setCo2Level(dto.getCo2Level())
                .build();
    }

    public LightSensorAvro toAvro(LightSensorEvent dto) {
        return LightSensorAvro.newBuilder()
                .setLinkQuality(dto.getLinkQuality())
                .setLuminosity(dto.getLuminosity())
                .build();
    }

    public MotionSensorAvro toAvro(MotionSensorEvent dto) {
        return MotionSensorAvro.newBuilder()
                .setLinkQuality(dto.getLinkQuality())
                .setMotion(dto.getMotion())
                .setVoltage(dto.getVoltage())
                .build();
    }

    public SwitchSensorAvro toAvro(SwitchSensorEvent dto) {
        return SwitchSensorAvro.newBuilder()
                .setState(dto.getState())
                .build();
    }

    public TemperatureSensorAvro toAvro(TemperatureSensorEvent dto) {
        return TemperatureSensorAvro.newBuilder()
                .setTemperatureC(dto.getTemperatureC())
                .setTemperatureF(dto.getTemperatureF())
                .build();
    }

    public DeviceAddedEventAvro toAvro(DeviceAddedEvent dto) {
        return DeviceAddedEventAvro.newBuilder()
                .setId(dto.getId())
                .setType(DeviceTypeAvro.valueOf(dto.getDeviceType().name()))
                .build();
    }

    public DeviceRemovedEventAvro toAvro(DeviceRemovedEvent dto) {
        return DeviceRemovedEventAvro.newBuilder()
                .setId(dto.getId())
                .build();
    }

    public ScenarioAddedEventAvro toAvro(ScenarioAddedEvent dto) {
        return ScenarioAddedEventAvro.newBuilder()
                .setName(dto.getName())
                .setConditions(dto.getConditions().stream().map(this::toAvro).collect(Collectors.toList()))
                .setActions(dto.getActions().stream().map(this::toAvro).collect(Collectors.toList()))
                .build();
    }

    public ScenarioConditionAvro toAvro(ScenarioCondition dto) {
        return ScenarioConditionAvro.newBuilder()
                .setSensorId(dto.getSensorId())
                .setType(ConditionTypeAvro.valueOf(dto.getType().name()))
                .setOperation(ConditionOperationAvro.valueOf(dto.getOperation().name()))
                .setValue(dto.getValue()) // Avro union [null, int, boolean] может принимать Integer напрямую
                .build();
    }

    public DeviceActionAvro toAvro(DeviceAction dto) {
        return DeviceActionAvro.newBuilder()
                .setSensorId(dto.getSensorId())
                .setType(ActionTypeAvro.valueOf(dto.getType().name()))
                .setValue(dto.getValue())
                .build();
    }

    public ScenarioRemovedEventAvro toAvro(ScenarioRemovedEvent dto) {
        return ScenarioRemovedEventAvro.newBuilder()
                .setName(dto.getName())
                .build();
    }
}

