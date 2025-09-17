package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.enums.QuantityState;
import ru.yandex.practicum.enums.ProductState;
import ru.yandex.practicum.exceptions.NotFoundException;
import ru.yandex.practicum.mapper.ProductMapper;
import ru.yandex.practicum.model.Product;
import ru.yandex.practicum.enums.ProductCategory;
import ru.yandex.practicum.repository.ProductRepository;

import java.util.UUID;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {
    private final ProductRepository productRepository;

    @Override
    public Page<ProductDto> getProducts(ProductCategory category, Pageable pageable) {
        Page<Product> products = productRepository.findAllByProductCategory(category, pageable);
        return products.map(ProductMapper::mapToProductDto);
    }

    @Override
    public ProductDto getProductById(UUID productId) {
        Product product = checkProductId(productId);
        return ProductMapper.mapToProductDto(product);
    }

    @Override
    @Transactional
    public ProductDto createProduct(ProductDto productDto) {
        Product productBeforeSaving = ProductMapper.mapFromRequest(productDto);
        Product productAfterSaving = productRepository.save(productBeforeSaving);
        return ProductMapper.mapToProductDto(productAfterSaving);
    }

    @Override
    @Transactional
    public ProductDto updateProduct(ProductDto productDto) {
        Product oldProduct = checkProductId(productDto.getProductId());
        Product updatedProduct = ProductMapper.updateProductFields(oldProduct, productDto);
        productRepository.save(updatedProduct);
        return ProductMapper.mapToProductDto(updatedProduct);
    }

    @Override
    @Transactional
    public boolean removeProduct(UUID productId) {
        Product product = checkProductId(productId);
        product.setProductState(ProductState.DEACTIVATE);
        productRepository.save(product);
        return true;
    }

    @Override
    @Transactional
    public boolean updateQuantityState(UUID productId, QuantityState quantityState) {
        Product product = checkProductId(productId);
        product.setQuantityState(quantityState);
        productRepository.save(product);
        return true;
    }

    private Product checkProductId(UUID productId) {
        return productRepository.findById(productId).orElseThrow(
                () -> new NotFoundException(String.format("Продукт id = %s не найден", productId))
        );
    }
}
