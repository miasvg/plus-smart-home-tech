package ru.yandex.practicum.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.BookedProductsDto;
import ru.yandex.practicum.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.enums.CartState;
import ru.yandex.practicum.exceptions.NoProductsInShoppingCartException;
import ru.yandex.practicum.exceptions.NotFoundException;
import ru.yandex.practicum.feign.WarehouseClient;
import ru.yandex.practicum.mapper.CartMapper;
import ru.yandex.practicum.model.ShoppingCart;
import ru.yandex.practicum.repository.CartRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final WarehouseClient warehouseClient;

    @Override
    @Transactional(readOnly = true)
    public ShoppingCartDto getShoppingCart(String username) {
        ShoppingCart cart = getOrCreateCart(username);
        return CartMapper.mapToCartDto(cart);
    }

    @Override
    public ShoppingCartDto addProduct(String username, Map<UUID, Integer> products) {
        ShoppingCart cart = getOrCreateCart(username);
        cartRepository.save(cart);

        mergeProducts(cart.getProducts(), products);

        checkWarehouseAvailability(cart);

        cart = cartRepository.save(cart);

        ShoppingCartDto result = CartMapper.mapToCartDto(cart);
        log.debug("Products added to user {}. Products overall: {}",
                username, result.getProducts().size());
        return result;
    }

    @Override
    public void deactivateCart(String username) {
        ShoppingCart cart = cartRepository.findByUsername(username).orElseThrow(
                () -> new NotFoundException(String.format("Cart for user %s not found", username))
        );

        if (cart.getCartState() != CartState.DEACTIVATE) {
            cart.setCartState(CartState.DEACTIVATE);
            cartRepository.save(cart);
            log.info("Cart of user {} deactivated", username);
        } else {
            log.debug("already Deactivated For: {}", username);
        }
    }

    @Override
    public ShoppingCartDto removeProduct(String username, Set<UUID> productIds) {
        ShoppingCart cart = checkShoppingCart(username);

        if (cart.getProducts() != null) {
            cart.getProducts().keySet().removeAll(productIds);
        }

        return CartMapper.mapToCartDto(cartRepository.save(cart));
    }

    @Override
    public ShoppingCartDto updateProductQuantity(String username, ChangeProductQuantityRequest requestDto) {
        ShoppingCart cart = checkShoppingCart(username);

        Map<UUID, Integer> products = cart.getProducts();
        if (products == null) {
            products = new HashMap<>();
            cart.setProducts(products);
        }

        UUID productId = requestDto.getProductId();
        Integer newQuantity = requestDto.getNewQuantity();

        if (!products.containsKey(productId)) {
            throw new NoProductsInShoppingCartException(String.format("Product with such id does not exist", productId));
        }

        if (newQuantity == 0) {
            products.remove(productId);
        } else {
            products.put(productId, newQuantity);
        }

        return CartMapper.mapToCartDto(cartRepository.save(cart));
    }

    private ShoppingCart checkShoppingCart(String username) {
        return cartRepository.findByUsernameAndCartState(username, CartState.ACTIVE).orElseThrow(
                () -> new NotFoundException(
                        String.format("No active carts for user %s", username)
                )
        );
    }

    private ShoppingCart getOrCreateCart(String username) {
        return cartRepository.findByUsername(username)
                .orElseGet(() -> {
                    log.debug("No cart for user {}. Creating New.", username);
                    return ShoppingCart.builder()
                            .username(username)
                            .cartState(CartState.ACTIVE)
                            .products(new HashMap<>())
                            .build();
                });
    }

    private void mergeProducts(Map<UUID, Integer> existingProducts, Map<UUID, Integer> newProducts) {
        if (existingProducts == null) {
            throw new IllegalStateException("Can not be null");
        }
        newProducts.forEach((productId, quantity) ->
                existingProducts.merge(productId, quantity, Integer::sum));
    }

    private void checkWarehouseAvailability(ShoppingCart cart) {
        try {
            log.info("checking: id={}, products={}",
                    cart.getShoppingCartId(), cart.getProducts());

            BookedProductsDto bookedProducts = warehouseClient
                    .checkProductQuantity(CartMapper.mapToCartDto(cart));

            log.info("checked: {}", bookedProducts);
        } catch (FeignException e) {
            log.error("Warehouse Error: {}", e.getMessage());
            throw new RuntimeException("Warehouse not available", e);
        }
    }
}
