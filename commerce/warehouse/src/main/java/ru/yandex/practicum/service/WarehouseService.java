package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.*;


public interface WarehouseService {

    void addProduct(NewProductInWarehouseRequest newProductInWarehouseRequest);

    BookedProductsDto checkProductQuantity(ShoppingCartDto shoppingCartDto);

    void updateProductQuantity(AddProductToWarehouseRequest addProductToWarehouseRequest);

    AddressDto getAddress();

}
