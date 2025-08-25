package ru.yandex.practicum.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.jpa_entities.Scenario;
import java.util.List;
import java.util.Optional;

public interface ScenarioRepository extends JpaRepository<Scenario, Long> {
    List<Scenario> findByHubId(String hubId);
    Optional<Scenario> findByHubIdAndName(String hubId, String name);

    @Query("SELECT s FROM Scenario s JOIN FETCH s.conditions c JOIN FETCH c.sensors WHERE s.hubId = :hubId")
    List<Scenario> findByHubIdWithConditionsAndSensors(@Param("hubId") String hubId);

    @Query("SELECT s FROM Scenario s JOIN FETCH s.actions a JOIN FETCH a.sensors WHERE s.hubId = :hubId")
    List<Scenario> findByHubIdWithActionsAndSensors(@Param("hubId") String hubId);
}
