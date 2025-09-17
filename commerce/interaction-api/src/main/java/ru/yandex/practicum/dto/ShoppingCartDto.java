package ru.yandex.practicum.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShoppingCartDto {
    @NotNull
    UUID shoppingCartId;

    @NotNull
    Map<UUID, Integer> products;
}
