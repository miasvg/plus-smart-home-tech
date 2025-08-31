package ru.yandex.practicum.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.jpa_entities.ScenarioCondition;
import ru.yandex.practicum.jpa_entities.ScenarioConditionId;

import java.util.List;

public interface ScenarioConditionRepository extends JpaRepository<ScenarioCondition, ScenarioConditionId> {
    @Query("SELECT sc FROM ScenarioCondition sc WHERE sc.scenario.id = :scenarioId")
    List<ScenarioCondition> findByScenarioId(@Param("scenarioId") Long scenarioId);
}
