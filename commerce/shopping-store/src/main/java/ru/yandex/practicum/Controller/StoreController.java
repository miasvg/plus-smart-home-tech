package ru.yandex.practicum.Controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.enums.QuantityState;
import ru.yandex.practicum.enums.ProductCategory;
import ru.yandex.practicum.service.StoreService;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shopping-store")
@RequiredArgsConstructor
public class StoreController implements StoreContract {
    private final StoreService storeService;

    @Override
    @GetMapping
    public Page<ProductDto> getProducts(@RequestParam ProductCategory category, Pageable pageable) {
        return storeService.getProducts(category, pageable);
    }

    @Override
    @GetMapping("/{product-id}")
    public ProductDto getProductById(@PathVariable(name = "product-id") @NotNull UUID productId) {
        return storeService.getProductById(productId);
    }

    @Override
    @PutMapping
    public ProductDto createProduct(@Valid @RequestBody ProductDto productDto) {
        return storeService.createProduct(productDto);
    }

    @Override
    @PostMapping
    public ProductDto updateProduct(@RequestBody ProductDto productDto) {
        return storeService.updateProduct(productDto);
    }

    @Override
    @PostMapping("/removeProductFromStore")
    public Boolean removeProduct(@RequestBody @NotNull UUID productId) {
        return storeService.removeProduct(productId);
    }

    @Override
    @PostMapping("/quantityState")
    public Boolean updateQuantityState(@RequestParam @NotNull UUID productId,
                                       @RequestParam @NotNull QuantityState quantityState) {
        return storeService.updateQuantityState(productId, quantityState);
    }
}
