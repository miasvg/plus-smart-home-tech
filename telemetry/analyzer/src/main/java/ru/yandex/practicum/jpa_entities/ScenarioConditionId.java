package ru.yandex.practicum.jpa_entities;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ScenarioConditionId implements Serializable {
    @Column(name = "scenario_id")
    private Long scenarioId;

    @Column(name = "condition_id")
    private Long conditionId;

    @Column(name = "sensor_id")
    private String sensorId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScenarioConditionId)) return false;
        ScenarioConditionId that = (ScenarioConditionId) o;
        return Objects.equals(scenarioId, that.scenarioId) &&
                Objects.equals(conditionId, that.conditionId) &&
                Objects.equals(sensorId, that.sensorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scenarioId, conditionId, sensorId);
    }
}

