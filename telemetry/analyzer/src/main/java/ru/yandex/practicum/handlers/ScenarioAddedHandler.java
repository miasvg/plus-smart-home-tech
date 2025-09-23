package ru.yandex.practicum.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.jpa_entities.Action;
import ru.yandex.practicum.jpa_entities.Condition;
import ru.yandex.practicum.jpa_entities.Scenario;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.repositories.ActionRepository;
import ru.yandex.practicum.repositories.ConditionRepository;
import ru.yandex.practicum.repositories.ScenarioRepository;
import ru.yandex.practicum.repositories.SensorRepository;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ScenarioAddedHandler implements HubEventHandler {

    private final ScenarioRepository scenarioRepository;
    private final ConditionRepository conditionRepository;
    private final ActionRepository actionRepository;
    private final SensorRepository sensorRepository;

    @Override
    @Transactional
    public void handle(HubEventAvro hubEvent) {
        ScenarioAddedEventAvro scenarioAddedEvent = (ScenarioAddedEventAvro) hubEvent.getPayload();
        Scenario scenario = scenarioRepository.findByHubIdAndName(hubEvent.getHubId(),
                scenarioAddedEvent.getName()).orElseGet(() -> scenarioRepository.save(buildToScenario(hubEvent)));

        if (checkSensorsInScenarioConditions(scenarioAddedEvent, hubEvent.getHubId())) {
            conditionRepository.saveAll(buildToCondition(scenarioAddedEvent, scenario));
        }
        if (checkSensorsInScenarioActions(scenarioAddedEvent, hubEvent.getHubId())) {
            actionRepository.saveAll(buildToAction(scenarioAddedEvent, scenario));
        }
    }

    @Override
    public String getMessageType() {
        return ScenarioAddedEventAvro.class.getSimpleName();
    }

    private Scenario buildToScenario(HubEventAvro hubEvent) {
        ScenarioAddedEventAvro scenarioAddedEvent = (ScenarioAddedEventAvro) hubEvent.getPayload();
        return Scenario.builder()
                .name(scenarioAddedEvent.getName())
                .hubId(hubEvent.getHubId())
                .build();
    }

    private Set<Condition> buildToCondition(ScenarioAddedEventAvro scenarioAddedEvent, Scenario scenario) {
        return scenarioAddedEvent.getConditions().stream()
                .map(c -> Condition.builder()
                        .sensor(sensorRepository.findById(c.getSensorId()).orElseThrow())
                        .scenario(scenario)
                        .type(c.getType())
                        .operation(c.getOperation())
                        .value(setConditionValue(c.getValue()))
                        .build())
                .collect(Collectors.toSet());
    }

    private Set<Action> buildToAction(ScenarioAddedEventAvro scenarioAddedEvent, Scenario scenario) {
        return scenarioAddedEvent.getActions().stream()
                .map(action -> Action.builder()
                        .sensor(sensorRepository.findById(action.getSensorId()).orElseThrow())
                        .scenario(scenario)
                        .type(action.getType())
                        .value(setActionValue(action.getValue()))
                        .build())
                .collect(Collectors.toSet());
    }

    private Integer setConditionValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer i) {
            return i;
        }
        if (value instanceof Boolean b) {
            return b ? 1 : 0;
        }
        throw new IllegalArgumentException("Unsupported condition value type: " + value.getClass());
    }

    private Integer setActionValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer i) {
            return i;
        }
        throw new IllegalArgumentException("Unsupported action value type: " + value.getClass());
    }


    private Boolean checkSensorsInScenarioConditions(ScenarioAddedEventAvro scenarioAddedEvent, String hubId) {
        return sensorRepository.existsByIdInAndHubId(scenarioAddedEvent.getConditions().stream()
                .map(ScenarioConditionAvro::getSensorId)
                .toList(), hubId);
    }

    private Boolean checkSensorsInScenarioActions(ScenarioAddedEventAvro scenarioAddedEvent, String hubId) {
        return sensorRepository.existsByIdInAndHubId(scenarioAddedEvent.getActions().stream()
                .map(DeviceActionAvro::getSensorId)
                .toList(), hubId);
    }
}