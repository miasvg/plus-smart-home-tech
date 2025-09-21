package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.BookedProductsDto;
import ru.yandex.practicum.dto.NewProductInWarehouseRequest;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.exceptions.NotFoundException;
import ru.yandex.practicum.exceptions.ProductInShoppingCartLowQuantityInWarehouseException;
import ru.yandex.practicum.exceptions.SpecifiedProductAlreadyInWarehouseException;
import ru.yandex.practicum.feign.ShoppingStoreClient;
import ru.yandex.practicum.mapper.WarehouseMapper;
import ru.yandex.practicum.model.Address;
import ru.yandex.practicum.model.Warehouse;
import ru.yandex.practicum.repository.WarehouseRepository;
import java.util.Map;
import java.util.UUID;
import ru.yandex.practicum.dto.AddProductToWarehouseRequest;
@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final ShoppingStoreClient shoppingStoreClient;

    @Override
    @Transactional
    public void addProduct(NewProductInWarehouseRequest requestDto) {
        UUID productId = requestDto.getProductId();

        if (warehouseRepository.existsById(productId)) {
            throw new SpecifiedProductAlreadyInWarehouseException(
                    String.format("Товар с ID = %s уже заведен на склад", productId));
        }

        Warehouse product = WarehouseMapper.mapFromRequest(requestDto);
        product.setQuantity(0);
        warehouseRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public BookedProductsDto checkProductQuantity(ShoppingCartDto cartDto) {
        double totalWeight = 0.0;
        double totalVolume = 0.0;
        boolean hasFragile = false;

        for (Map.Entry<UUID, Integer> entry : cartDto.getProducts().entrySet()) {
            UUID productId = entry.getKey();
            long requestedQty = entry.getValue();

            Warehouse product = warehouseRepository.findById(productId).orElseThrow(
                    () -> new NotFoundException(String.format("Продукт id = %s не найден на складе", productId))
            );

            if (product.getQuantity() < requestedQty) {
                throw new ProductInShoppingCartLowQuantityInWarehouseException(
                        String.format("На складе недостаточно товара id = %s", productId));
            }

            totalWeight += product.getWeight() * requestedQty;
            totalVolume += product.getParametersDto().getWidth() *
                    product.getParametersDto().getHeight() *
                    product.getParametersDto().getDepth() * requestedQty;

            if (product.getFragile()) hasFragile = true;
        }

        return BookedProductsDto.builder()
                .deliveryWeight(totalWeight)
                .deliveryVolume(totalVolume)
                .fragile(hasFragile)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AddressDto getAddress() {
        String address = Address.CURRENT_ADDRESS;
        return AddressDto.builder()
                .country(address)
                .city(address)
                .street(address)
                .house(address)
                .flat(address)
                .build();
    }

    @Override
    @Transactional
    public void updateProductQuantity(AddProductToWarehouseRequest requestDto) {
        UUID productId = requestDto.getProductId();
        Warehouse product = warehouseRepository.findById(productId).orElseThrow(
                () -> new NotFoundException(String.format("Продукт id = %s не найден на складе", productId))
        );
        product.setQuantity(product.getQuantity() + requestDto.getQuantity());
        warehouseRepository.save(product);
    }
}