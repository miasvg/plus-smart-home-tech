package ru.yandex.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.yandex.practicum.jpa_entities.Condition;
import ru.yandex.practicum.jpa_entities.Sensor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConditionSensor {
    private Condition condition;
    private Sensor sensor;
}