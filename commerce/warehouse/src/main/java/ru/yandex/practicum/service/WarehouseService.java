package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.*;

import java.util.Map;
import java.util.UUID;


public interface WarehouseService {

    void addProduct(NewProductInWarehouseRequest newProductInWarehouseRequest);

    BookedProductsDto checkProductQuantity(ShoppingCartDto shoppingCartDto);

    void updateProductQuantity(AddProductToWarehouseRequest addProductToWarehouseRequest);

    AddressDto getAddress();

    void shippedToDelivery(ShippedToDeliveryRequest deliveryRequest);

    void acceptReturn(Map<UUID, Long> products);

    BookedProductsDto assemblyProductsForOrder(AssemblyProductsForOrderRequest assemblyProductsForOrder);

    BookedProductsDto bookingProducts(ShoppingCartDto shoppingCartDto);

}
