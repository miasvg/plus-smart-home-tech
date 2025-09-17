package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.enums.ProductCategory;
import ru.yandex.practicum.enums.ProductState;
import ru.yandex.practicum.enums.QuantityState;

import java.util.UUID;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "product_id", updatable = false, nullable = false)
    UUID productId;

    @Column(name = "product_name", nullable = false)
    String productName;

    @Column(name = "description", nullable = false)
    String description;

    @Column(name = "image_src")
    String imageSrc;

    @Column(name = "quantity_state")
    @Enumerated(EnumType.STRING)
    QuantityState quantityState;

    @Column(name = "product_state")
    @Enumerated(EnumType.STRING)
    ProductState productState;

    @Column(name = "product_category")
    @Enumerated(EnumType.STRING)
    ProductCategory productCategory;

    @Column(name = "price", nullable = false)
    Float price;
}
