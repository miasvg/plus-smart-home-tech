package ru.yandex.practicum.processors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.ConditionOperation;
import ru.yandex.practicum.dto.ConditionType;
import ru.yandex.practicum.jpa_entities.Action;
import ru.yandex.practicum.jpa_entities.Condition;
import ru.yandex.practicum.jpa_entities.Scenario;
import ru.yandex.practicum.jpa_entities.Sensor;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.service.HubRouterClientService;

import java.util.Objects;

import static ru.yandex.practicum.dto.ConditionOperation.*;
import static ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro.*;


@Slf4j
@Component
@RequiredArgsConstructor
public class ScenarioChecker {
    private final HubRouterClientService hubRouterClientService;

    /**
     * Проверяет все условия сценария против текущего снапшота
     */
    public boolean checkConditions(Scenario scenario, SensorsSnapshotAvro snapshot) {
        // теперь scenario.getConditions() содержит ScenarioCondition
        return scenario.getConditions().stream()
                .allMatch(sc -> checkConditionForSensor(sc.getCondition(), sc.getSensor(), snapshot));
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
    private Object extractSensorValue(SensorStateAvro sensorState, ConditionType conditionType) {
        Object sensorData = sensorState.getData();

        try {
            switch (conditionType) {
                case TEMPERATURE:
                    if (sensorData instanceof TemperatureSensorAvro t) {
                        return t.getTemperatureC();
                    }
                    if (sensorData instanceof ClimateSensorAvro c) {
                        return c.getTemperatureC();
                    }
                    break;
                case HUMIDITY:
                    if (sensorData instanceof ClimateSensorAvro c) return c.getHumidity();
                    break;
                case CO2LEVEL:
                    if (sensorData instanceof ClimateSensorAvro c) return c.getCo2Level();
                    break;
                case LUMINOSITY:
                    if (sensorData instanceof LightSensorAvro l) return l.getLuminosity();
                    break;
                case MOTION:
                    if (sensorData instanceof MotionSensorAvro m) return m.getMotion();
                    break;
                case SWITCH:
                    if (sensorData instanceof SwitchSensorAvro s) return s.getState();
                    break;
            }
        } catch (Exception e) {
            log.error("Error extracting sensor value for type {}", conditionType, e);
        }

        return null;
    }

    private boolean evaluateCondition(Condition condition, Object sensorValue) {
        if (sensorValue == null || condition.getValue() == null) return false;

        try {
            if (sensorValue instanceof Boolean actualBool) {
                boolean expected = condition.getValue() != 0; // если value хранится как int
                return condition.getOperation() == ConditionOperation.EQUALS && actualBool == expected;
            }

            if (sensorValue instanceof Integer actualInt) {
                int expected = condition.getValue();
                switch (condition.getOperation()) {
                    case EQUALS -> { return actualInt == expected; }
                    case GREATER_THAN -> { return actualInt > expected; }
                    case LOWER_THAN -> { return actualInt < expected; }
                    default -> { return false; }
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
        scenario.getActions().forEach(sa -> {
            try {
                Action action = sa.getAction();
                Sensor sensor = sa.getSensor();
                hubRouterClientService.sendDeviceCommand(
                        hubId,
                        scenario.getName(),
                        sensor.getId(),
                        action.getType().name(),
                        action.getValue()
                );
            } catch (Exception e) {
                log.error("❌ Failed to execute action for scenario {} sensor {}",
                        scenario.getName(), sa.getSensor().getId(), e);
            }
        });
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
