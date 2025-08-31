package ru.yandex.practicum.jpa_entities;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.dto.ActionType;
import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "actions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Action {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ActionType type;

    @Column(name = "value", nullable = true)
    private Integer value;
}