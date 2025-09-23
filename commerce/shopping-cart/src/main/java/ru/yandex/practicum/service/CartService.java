package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.ShoppingCartDto;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface CartService {

    ShoppingCartDto getShoppingCart(String username);

    ShoppingCartDto addProduct(String username, Map<UUID, Integer> request);

    void deactivateCart(String username);

    ShoppingCartDto removeProduct(String username, Set<UUID> productsId);

    ShoppingCartDto updateProductQuantity(String username, ChangeProductQuantityRequest requestDto);

}
