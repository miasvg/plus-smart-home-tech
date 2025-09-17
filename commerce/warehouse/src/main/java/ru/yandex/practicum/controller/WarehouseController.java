package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.AddProductToWarehouseRequest;
import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.BookedProductsDto;
import ru.yandex.practicum.dto.NewProductInWarehouseRequest;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.service.WarehouseService;

@RestController
@RequestMapping("/api/v1/warehouse")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @PutMapping
    public void addProduct(@RequestBody @Valid NewProductInWarehouseRequest requestDto) {
        warehouseService.addProduct(requestDto);
    }

    @PostMapping("/check")
    public BookedProductsDto checkProductQuantity(@RequestBody @Valid ShoppingCartDto cartDto) {
        return warehouseService.checkProductQuantity(cartDto);
    }

    @PostMapping("/add")
    public void updateProductQuantity(@RequestBody @Valid AddProductToWarehouseRequest requestDto) {
        warehouseService.updateProductQuantity(requestDto);
    }

    @GetMapping("/address")
    public AddressDto getAddress() {
        return warehouseService.getAddress();
    }
}
