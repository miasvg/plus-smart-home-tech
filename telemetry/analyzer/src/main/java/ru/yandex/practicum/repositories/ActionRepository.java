package ru.yandex.practicum.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.jpa_entities.Action;
import ru.yandex.practicum.jpa_entities.Scenario;

import java.util.List;

public interface ActionRepository extends JpaRepository<Action, Long> {

    List<Action> findAllByScenarioIn(List<Scenario> scenario);

    void deleteByScenario(Scenario scenario);
}
