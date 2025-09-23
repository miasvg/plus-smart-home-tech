package ru.yandex.practicum.mapper;

import ru.yandex.practicum.model.ShoppingCart;
import ru.yandex.practicum.dto.ShoppingCartDto;
public class CartMapper {

    public static ShoppingCartDto mapToCartDto(ShoppingCart shoppingCart) {
        return ShoppingCartDto.builder()
                .shoppingCartId(shoppingCart.getShoppingCartId())
                .products(shoppingCart.getProducts())
                .build();
    }
}
