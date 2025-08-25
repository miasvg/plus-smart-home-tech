package ru.yandex.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.yandex.practicum.jpa_entities.Action;
import ru.yandex.practicum.jpa_entities.Sensor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActionSensor {
    private Action action;
    private Sensor sensor;
}
