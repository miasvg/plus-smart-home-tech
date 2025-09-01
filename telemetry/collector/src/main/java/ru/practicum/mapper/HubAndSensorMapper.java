package ru.practicum.mapper;

import com.google.protobuf.Timestamp;
import org.springframework.stereotype.Component;
import ru.practicum.telemetry.message.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;
import java.util.stream.Collectors;

@Component
public class HubAndSensorMapper {

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


