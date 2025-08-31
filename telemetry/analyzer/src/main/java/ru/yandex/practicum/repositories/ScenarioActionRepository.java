package ru.yandex.practicum.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.jpa_entities.ScenarioAction;
import ru.yandex.practicum.jpa_entities.ScenarioActionId;

import java.util.List;

public interface ScenarioActionRepository extends JpaRepository<ScenarioAction, ScenarioActionId> {
    @Query("SELECT sa FROM ScenarioAction sa WHERE sa.scenario.id = :scenarioId")
    List<ScenarioAction> findByScenarioId(@Param("scenarioId") Long scenarioId);
}
