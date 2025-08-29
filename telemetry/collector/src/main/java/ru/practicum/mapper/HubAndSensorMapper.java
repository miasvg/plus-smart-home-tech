package ru.practicum.mapper;

import com.google.protobuf.Timestamp;
import org.springframework.stereotype.Component;
import ru.practicum.dto.hubs.*;
import ru.practicum.dto.hubs.HubEvent;
import ru.practicum.dto.sensors.*;
import ru.practicum.dto.sensors.ClimateSensorEvent;
import ru.practicum.dto.sensors.LightSensorEvent;
import ru.practicum.dto.sensors.MotionSensorEvent;
import ru.practicum.dto.sensors.SensorEvent;
import ru.practicum.dto.sensors.SwitchSensorEvent;
import ru.practicum.dto.sensors.TemperatureSensorEvent;
import ru.practicum.telemetry.message.*;
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
                .setTimestamp(dto.getTimestamp());

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
        ScenarioConditionAvro.Builder builder = ScenarioConditionAvro.newBuilder()
                .setSensorId(dto.getSensorId())
                .setType(ConditionTypeAvro.valueOf(dto.getType().name()))
                .setOperation(ConditionOperationAvro.valueOf(dto.getOperation().name()));

        if (dto.getValue() == null) {
            builder.setValue(null);
        } else if (dto.getValue() instanceof Boolean) {
            builder.setValue((Boolean) dto.getValue());
        } else if (dto.getValue() instanceof Integer) {
            builder.setValue((Integer) dto.getValue());
        } else if (dto.getValue() instanceof Long) {
            builder.setValue((Long) dto.getValue());
        } else {
            throw new IllegalStateException("Неподдерживаемый тип значения: " +
                    dto.getValue().getClass().getName());
        }

        return builder.build();
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

        // === Преобразование SensorEventProto → SensorEventAvro ===

        public SensorEventAvro toAvro(SensorEventProto proto) {
            SensorEventAvro.Builder builder = SensorEventAvro.newBuilder()
                    .setId(proto.getId())
                    .setHubId(proto.getHubId())
                    .setTimestamp(Instant.ofEpochSecond(proto.getTimestamp().getSeconds(), proto.getTimestamp().getNanos()));

            switch (proto.getPayloadCase()) {
                case MOTION_SENSOR_EVENT:
                    builder.setPayload(toAvro(proto.getMotionSensorEvent()));
                    break;
                case TEMPERATURE_SENSOR_EVENT:
                    builder.setPayload(toAvro(proto.getTemperatureSensorEvent()));
                    break;
                case LIGHT_SENSOR_EVENT:
                    builder.setPayload(toAvro(proto.getLightSensorEvent()));
                    break;
                case CLIMATE_SENSOR_EVENT:
                    builder.setPayload(toAvro(proto.getClimateSensorEvent()));
                    break;
                case SWITCH_SENSOR_EVENT:
                    builder.setPayload(toAvro(proto.getSwitchSensorEvent()));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown payload type: " + proto.getPayloadCase());
            }
            return builder.build();
        }

        private MotionSensorAvro toAvro(MotionSensorEventProto proto) {
            return MotionSensorAvro.newBuilder()
                    .setLinkQuality(proto.getLinkQuality())
                    .setMotion(proto.getMotion())
                    .setVoltage(proto.getVoltage())
                    .build();
        }

        private TemperatureSensorAvro toAvro(TemperatureSensorEventProto proto) {
            return TemperatureSensorAvro.newBuilder()
                    .setTemperatureC(proto.getTemperatureC())
                    .setTemperatureF(proto.getTemperatureF())
                    .build();
        }

        private LightSensorAvro toAvro(LightSensorEventProto proto) {
            return LightSensorAvro.newBuilder()
                    .setLinkQuality(proto.getLinkQuality())
                    .setLuminosity(proto.getLuminosity())
                    .build();
        }

        private ClimateSensorAvro toAvro(ClimateSensorEventProto proto) {
            return ClimateSensorAvro.newBuilder()
                    .setTemperatureC(proto.getTemperatureC())
                    .setHumidity(proto.getHumidity())
                    .setCo2Level(proto.getCo2Level())
                    .build();
        }

        private SwitchSensorAvro toAvro(SwitchSensorEventProto proto) {
            return SwitchSensorAvro.newBuilder()
                    .setState(proto.getState())
                    .build();
        }

        // === Преобразование HubEventProto → HubEventAvro ===

        public HubEventAvro toAvro(HubEventProto proto) {
            HubEventAvro.Builder builder = HubEventAvro.newBuilder()
                    .setHubId(proto.getHubId())
                    .setTimestamp(Instant.ofEpochSecond(proto.getTimestamp().getSeconds(), proto.getTimestamp().getNanos()));

            switch (proto.getPayloadCase()) {
                case DEVICE_ADDED:
                    builder.setPayload(toAvro(proto.getDeviceAdded()));
                    break;
                case DEVICE_REMOVED:
                    builder.setPayload(toAvro(proto.getDeviceRemoved()));
                    break;
                case SCENARIO_ADDED:
                    builder.setPayload(toAvro(proto.getScenarioAdded()));
                    break;
                case SCENARIO_REMOVED:
                    builder.setPayload(toAvro(proto.getScenarioRemoved()));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown payload type: " + proto.getPayloadCase());
            }
            return builder.build();
        }

        private DeviceAddedEventAvro toAvro(DeviceAddedEventProto proto) {
            return DeviceAddedEventAvro.newBuilder()
                    .setId(proto.getId())
                    .setType(DeviceTypeAvro.valueOf(proto.getType().name()))
                    .build();
        }

        private DeviceRemovedEventAvro toAvro(DeviceRemovedEventProto proto) {
            return DeviceRemovedEventAvro.newBuilder()
                    .setId(proto.getId())
                    .build();
        }

        private ScenarioAddedEventAvro toAvro(ScenarioAddedEventProto proto) {
            return ScenarioAddedEventAvro.newBuilder()
                    .setName(proto.getName())
                    .setConditions(proto.getConditionList().stream()
                            .map(this::toAvro)
                            .collect(Collectors.toList()))
                    .setActions(proto.getActionList().stream()
                            .map(this::toAvro)
                            .collect(Collectors.toList()))
                    .build();
        }

        private ScenarioConditionAvro toAvro(ScenarioConditionProto proto) {
            ScenarioConditionAvro.Builder builder = ScenarioConditionAvro.newBuilder()
                    .setSensorId(proto.getSensorId())
                    .setType(ConditionTypeAvro.valueOf(proto.getType().name()))
                    .setOperation(ConditionOperationAvro.valueOf(proto.getOperation().name()));

            switch (proto.getValueCase()) {
                case BOOL_VALUE:
                    builder.setValue(proto.getBoolValue());
                    break;
                case INT_VALUE:
                    builder.setValue(proto.getIntValue());
                    break;
                default:
                    builder.setValue(null);
            }
            return builder.build();
        }

        private DeviceActionAvro toAvro(DeviceActionProto proto) {
            return DeviceActionAvro.newBuilder()
                    .setSensorId(proto.getSensorId())
                    .setType(ActionTypeAvro.valueOf(proto.getType().name()))
                    .setValue(proto.hasValue() ? proto.getValue() : null)
                    .build();
        }

        private ScenarioRemovedEventAvro toAvro(ScenarioRemovedEventProto proto) {
            return ScenarioRemovedEventAvro.newBuilder()
                    .setName(proto.getName())
                    .build();
        }
    }


