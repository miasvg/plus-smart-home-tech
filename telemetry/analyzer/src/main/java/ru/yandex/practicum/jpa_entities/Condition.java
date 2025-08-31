package ru.yandex.practicum.jpa_entities;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.dto.ConditionOperation;
import ru.yandex.practicum.dto.ConditionType;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "conditions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Condition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ConditionType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation", nullable = false)
    private ConditionOperation operation;

    @Column(name = "value", nullable = true)
    private Integer value;
}
