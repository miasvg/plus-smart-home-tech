package ru.yandex.practicum.mapper;

import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.model.Product;

public class ProductMapper {

    public static ProductDto mapToProductDto(Product product) {
        return ProductDto.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .description(product.getDescription())
                .imageSrc(product.getImageSrc())
                .quantityState(product.getQuantityState())
                .productState(product.getProductState())
                .productCategory(product.getProductCategory())
                .price(product.getPrice())
                .build();
    }

    public static Product mapFromRequest(ProductDto productDto) {
        return Product.builder()
                .productName(productDto.getProductName())
                .description(productDto.getDescription())
                .imageSrc(productDto.getImageSrc())
                .quantityState(productDto.getQuantityState())
                .productState(productDto.getProductState())
                .productCategory(productDto.getProductCategory())
                .price(productDto.getPrice())
                .build();
    }

    public static Product updateProductFields(Product product, ProductDto productDto) {
        if (productDto.getProductName() != null && !productDto.getProductName().isBlank()) {
            product.setProductName(productDto.getProductName());
        }
        if (productDto.getDescription() != null && !productDto.getDescription().isBlank()) {
            product.setDescription(productDto.getDescription());
        }
        if (productDto.getImageSrc() != null && !productDto.getImageSrc().isBlank()) {
            product.setImageSrc(productDto.getImageSrc());
        }
        if (productDto.getQuantityState() != null) {
            product.setQuantityState(productDto.getQuantityState());
        }
        if (productDto.getProductState() != null) {
            product.setProductState(productDto.getProductState());
        }
        if (productDto.getProductCategory() != null) {
            product.setProductCategory(productDto.getProductCategory());
        }
        if (productDto.getPrice() != null) {
            product.setPrice(productDto.getPrice());
        }
        return product;
    }
}