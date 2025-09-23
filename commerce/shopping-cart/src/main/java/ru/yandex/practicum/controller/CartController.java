package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.service.CartService;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shopping-cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ShoppingCartDto getShoppingCart(@RequestParam @NotBlank String username) {
        return cartService.getShoppingCart(username);
    }

    @PutMapping
    public ShoppingCartDto addProduct(@RequestParam @NotBlank String username,
                                      @RequestBody @NotNull @NotEmpty Map<UUID, Integer> request) {
        return cartService.addProduct(username, request);
    }

    @DeleteMapping
    public void deactivateCart(@RequestParam @NotBlank String username) {
        cartService.deactivateCart(username);
    }

    @PostMapping("/remove")
    public ShoppingCartDto removeProduct(@RequestParam @NotBlank String username,
                                         @RequestBody @NotNull @NotEmpty Set<UUID> productsId) {
        return cartService.removeProduct(username, productsId);
    }

    @PostMapping("/change-quantity")
    public ShoppingCartDto updateProductQuantity(@RequestParam @NotBlank String username,
                                                 @Valid @RequestBody ChangeProductQuantityRequest requestDto) {
        return cartService.updateProductQuantity(username, requestDto);
    }
}

