package ru.yandex.practicum.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.jpa_entities.Condition;
import ru.yandex.practicum.jpa_entities.Scenario;

import java.util.List;

public interface ConditionRepository extends JpaRepository<Condition, Long> {

    void deleteByScenario(Scenario scenario);

    List<Condition> findAllByScenario(Scenario scenario);
}