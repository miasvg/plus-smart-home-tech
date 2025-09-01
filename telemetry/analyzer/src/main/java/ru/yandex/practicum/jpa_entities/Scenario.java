package ru.yandex.practicum.jpa_entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "scenarios")
@Getter @Setter @Builder
@NoArgsConstructor
@AllArgsConstructor
public class Scenario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hub_id", nullable = false)
    private String hubId;

    @Column(name = "name", nullable = false)
    private String name;
}
