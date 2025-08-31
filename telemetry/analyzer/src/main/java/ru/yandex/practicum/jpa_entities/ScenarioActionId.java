package ru.yandex.practicum.jpa_entities;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ScenarioActionId implements Serializable {
    @Column(name = "scenario_id")
    private Long scenarioId;

    @Column(name = "action_id")
    private Long actionId;

    @Column(name = "sensor_id")
    private String sensorId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScenarioActionId)) return false;
        ScenarioActionId that = (ScenarioActionId) o;
        return Objects.equals(scenarioId, that.scenarioId) &&
                Objects.equals(actionId, that.actionId) &&
                Objects.equals(sensorId, that.sensorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scenarioId, actionId, sensorId);
    }
}

