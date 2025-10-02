package ru.yandex.practicum.mapper;

import ru.yandex.practicum.dto.NewProductInWarehouseRequest;
import ru.yandex.practicum.model.Warehouse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;


@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface WarehouseMapper {

    static Warehouse toWarehouse(NewProductInWarehouseRequest newProductInWarehouseRequest);
}
