package ru.yandex.practicum.processors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.jpa_entities.Action;
import ru.yandex.practicum.jpa_entities.Condition;
import ru.yandex.practicum.jpa_entities.Scenario;
import ru.yandex.practicum.jpa_entities.Sensor;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.service.HubRouterClientService;


@Slf4j
@Component
@RequiredArgsConstructor
public class ScenarioChecker {
    private final HubRouterClientService hubRouterClientService;

    /**
     * Проверяет все условия сценария против текущего снапшота
     */
    public boolean checkConditions(Scenario scenario, SensorsSnapshotAvro snapshot) {
        return scenario.getConditions().stream()
                .allMatch(condition -> checkCondition(condition, snapshot));
    }

    /**
     * Проверяет одно условие для всех связанных сенсоров
     */
    private boolean checkCondition(Condition condition, SensorsSnapshotAvro snapshot) {
        return condition.getSensors().stream()
                .allMatch(sensor -> checkConditionForSensor(condition, sensor, snapshot));
    }

    /**
     * Проверяет условие для конкретного сенсора
     */
    private boolean checkConditionForSensor(Condition condition, Sensor sensor, SensorsSnapshotAvro snapshot) {
        SensorStateAvro sensorState = snapshot.getSensorsState().get(sensor.getId());

        if (sensorState == null || sensorState.getData() == null) {
            log.debug("Sensor {} not found or has no data in snapshot", sensor.getId());
            return false;
        }

        Object sensorValue = extractSensorValue(sensorState, condition.getType());
        if (sensorValue == null) {
            log.debug("Could not extract value for sensor {} with type {}", sensor.getId(), condition.getType());
            return false;
        }

        boolean result = evaluateCondition(condition, sensorValue);
        log.debug("Condition check: sensor={}, type={}, operation={}, value={}, actual={}, result={}",
                sensor.getId(), condition.getType(), condition.getOperation(), condition.getValue(),
                sensorValue, result);

        return result;
    }

    /**
     * Извлекает значение из данных сенсора по типу условия
     */
    private Object extractSensorValue(SensorStateAvro sensorState, ConditionTypeAvro type) {
        Object sensorData = sensorState.getData();

        try {
            switch (type) {
                case TEMPERATURE:
                    if (sensorData instanceof ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro) {
                        return ((ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro) sensorData).getTemperatureC();
                    }
                    if (sensorData instanceof ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro) {
                        return ((ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro) sensorData).getTemperatureC();
                    }
                    break;

                case HUMIDITY:
                    if (sensorData instanceof ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro) {
                        return ((ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro) sensorData).getHumidity();
                    }
                    break;

                case CO2LEVEL:
                    if (sensorData instanceof ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro) {
                        return ((ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro) sensorData).getCo2Level();
                    }
                    break;

                case LUMINOSITY:
                    if (sensorData instanceof ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro) {
                        return ((ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro) sensorData).getLuminosity();
                    }
                    break;

                case MOTION:
                    if (sensorData instanceof ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro) {
                        return ((ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro) sensorData).getMotion();
                    }
                    break;

                case SWITCH:
                    if (sensorData instanceof ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro) {
                        return ((ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro) sensorData).getState();
                    }
                    break;
            }
        } catch (Exception e) {
            log.error("Error extracting sensor value for type {}", type, e);
        }

        return null;
    }

    /**
     * Выполняет сравнение значения сенсора с условием
     */
    private boolean evaluateCondition(Condition condition, Object sensorValue) {
        if (sensorValue == null || condition.getValue() == null) {
            return false;
        }

        try {
            if (sensorValue instanceof Boolean) {
                boolean actual = (Boolean) sensorValue;
                boolean expected = condition.getValue() != 0;

                return condition.getOperation() == ConditionOperationAvro.EQUALS && actual == expected;
            }

            if (sensorValue instanceof Integer) {
                int actual = (Integer) sensorValue;
                int expected = condition.getValue();

                switch (condition.getOperation()) {
                    case EQUALS:
                        return actual == expected;
                    case GREATER_THAN:
                        return actual > expected;
                    case LOWER_THAN:
                        return actual < expected;
                    default:
                        return false;
                }
            }

            return false;

        } catch (Exception e) {
            log.error("Error evaluating condition", e);
            return false;
        }
    }

    /**
     * Выполняет все действия сценария
     */
    public void executeActions(Scenario scenario, String hubId) {
        if (scenario.getActions().isEmpty()) {
            log.warn("Scenario '{}' has no actions to execute", scenario.getName());
            return;
        }

        log.info("Executing {} actions for scenario '{}' in hub {}",
                scenario.getActions().size(), scenario.getName(), hubId);

        scenario.getActions().forEach(action ->
                action.getSensors().forEach(sensor ->
                        executeAction(action, sensor, hubId, scenario.getName())));
    }

    /**
     * Выполняет одно действие для конкретного сенсора
     */
    private void executeAction(Action action, Sensor sensor, String hubId, String scenarioName) {
        try {
            hubRouterClientService.sendDeviceCommand(
                    hubId,
                    scenarioName,
                    sensor.getId(),
                    action.getType().name(),
                    action.getValue()
            );

        } catch (Exception e) {
            log.error("❌ Failed to execute action: {} for sensor: {} in scenario: {}",
                    action.getType(), sensor.getId(), scenarioName, e);
        }
    }
}
