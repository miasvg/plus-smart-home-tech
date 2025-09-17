package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.dto.ProductParametersDto;

import java.util.UUID;

@Entity
@Table(name = "warehouse_products")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Warehouse {
    @Id
    @Column(name = "product_id", nullable = false, unique = true)
    UUID productId;

    Boolean fragile;

    @Embedded
    ProductParametersDto parametersDto;

    @Column(name = "weight", nullable = false)
    Double weight;

    @Column(name = "quantity", nullable = false)
    Integer quantity;
}