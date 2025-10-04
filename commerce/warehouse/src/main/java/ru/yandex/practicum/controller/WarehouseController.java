package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.service.WarehouseService;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/warehouse")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @PutMapping
    public void addProduct(@RequestBody @Valid NewProductInWarehouseRequest requestDto) {
        warehouseService.addProduct(requestDto);
    }

    @PostMapping("/shipped")
    public void shippedToDelivery(ShippedToDeliveryRequest deliveryRequest) {
        try {
            log.info("Передать товары в доставку {}", deliveryRequest);
            warehouseService.shippedToDelivery(deliveryRequest);
        } catch (Exception e) {
            log.error("Ошибка передачи товаров в доставку.");
            throw e;
        }
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

    @PostMapping("/return")
    public void acceptReturn(@RequestBody Map<UUID, Long> products) {
        try {
            log.info("Принять возврат товаров на склад {}", products);
            warehouseService.acceptReturn(products);
        } catch (Exception e) {
            log.error("Ошибка возврата товаров на склад.");
            throw e;
        }
    }
    @PostMapping("/assembly")
    public BookedProductsDto assemblyProductsForOrder(@RequestBody @Valid AssemblyProductsForOrderRequest assemblyProductsForOrder) {
        try {
            log.info("Собрать товары к заказу для подготовки к отправке {}",  assemblyProductsForOrder);
            return warehouseService.assemblyProductsForOrder(assemblyProductsForOrder);
        } catch (Exception e) {
            log.error("Ошибка сборки товаров.");
            throw e;
        }
    }

    @PostMapping("/booking")
    public BookedProductsDto bookingProducts(@RequestBody @Valid ShoppingCartDto shoppingCartDto) {
        try {
            log.info("Бронирование корзины покупок {}", shoppingCartDto);
            return warehouseService.bookingProducts(shoppingCartDto);
        } catch (Exception e) {
            log.error("Ошибка бронирования корзины покупок.");
            throw e;
        }
    }
}
