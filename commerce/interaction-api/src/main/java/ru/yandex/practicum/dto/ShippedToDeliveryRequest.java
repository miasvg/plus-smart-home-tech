package ru.yandex.practicum.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShippedToDeliveryRequest {
    @NotNull
    UUID orderId;

    @NotNull
    UUID deliveryId;
}
