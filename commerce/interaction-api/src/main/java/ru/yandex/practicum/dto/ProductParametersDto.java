package ru.yandex.practicum.dto;

import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductParametersDto {
    @Positive
    Double width;
    @Positive
    Double height;
    @Positive
    Double depth;
}
