package ru.yandex.practicum.dto;

import lombok.*;
import ru.yandex.practicum.jpa_entities.Scenario;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ScenarioDto {
    private Scenario scenario;
    private List<ConditionSensorDto> conditions;
    private List<ActionSensorDto> actions;
}

