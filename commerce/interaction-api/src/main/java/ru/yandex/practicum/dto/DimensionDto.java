package ru.yandex.practicum.dto;

import jakarta.validation.constraints.Min;

public class DimensionDto {
    @Min(1)
    Double width;

    @Min(1)
    Double height;

    @Min(1)
    Double depth;
}
