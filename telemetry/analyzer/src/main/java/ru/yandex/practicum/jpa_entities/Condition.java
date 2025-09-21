package ru.yandex.practicum.jpa_entities;

import jakarta.persistence.*;
import lombok.*;

import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;

@Entity
@Table(name = "conditions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@SecondaryTable(name = "scenario_conditions", pkJoinColumns = @PrimaryKeyJoinColumn(name = "condition_id"))
public class Condition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
     Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
     ConditionTypeAvro type;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation", nullable = false)
    ConditionOperationAvro operation;

    @Column(name = "value", nullable = true)
    Integer value;

    @ManyToOne
    @JoinColumn(name = "scenario_id", table = "scenario_conditions")
    Scenario scenario;

    @ManyToOne
    @JoinColumn(name = "sensor_id", table = "scenario_conditions")
    Sensor sensor;
}
