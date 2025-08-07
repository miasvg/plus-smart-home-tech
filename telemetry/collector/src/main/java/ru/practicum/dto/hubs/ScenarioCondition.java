package ru.practicum.dto.hubs;

import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScenarioCondition {
    private String sensorId;
    private ConditionType type;
    private ConditionOperation operation;

    private Object value;
}