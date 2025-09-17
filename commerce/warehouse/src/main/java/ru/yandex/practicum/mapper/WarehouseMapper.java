package ru.yandex.practicum.mapper;


import ru.yandex.practicum.dto.NewProductInWarehouseRequest;
import ru.yandex.practicum.dto.ProductParametersDto;
import ru.yandex.practicum.model.Warehouse;


public class WarehouseMapper {

    public static Warehouse mapFromRequest(NewProductInWarehouseRequest request) {
        return Warehouse.builder()
                .productId(request.getProductId())
                .fragile(request.getFragile())
                .parametersDto(ProductParametersDto.builder()
                        .width(request.getDimension().getWidth())
                        .height(request.getDimension().getHeight())
                        .depth(request.getDimension().getDepth())
                        .build())
                .weight(request.getWeight())
                .build();
    }

}
