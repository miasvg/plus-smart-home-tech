package ru.yandex.practicum.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.jpa_entities.Action;

public interface ActionRepository extends JpaRepository<Action, Long> {
}
