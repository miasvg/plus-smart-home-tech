package ru.yandex.practicum.jpa_entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "scenarios")
@Getter @Setter @Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Scenario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
     Long id;

    @Column(name = "hub_id", nullable = false)
     String hubId;

    @Column(name = "name", nullable = false)
     String name;
}
