package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.exceptions.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.exceptions.NotFoundException;
import ru.yandex.practicum.exceptions.ProductInShoppingCartLowQuantityInWarehouseException;
import ru.yandex.practicum.exceptions.SpecifiedProductAlreadyInWarehouseException;
import ru.yandex.practicum.feign.ShoppingStoreClient;
import ru.yandex.practicum.mapper.BookingMapper;
import ru.yandex.practicum.mapper.WarehouseMapper;
import ru.yandex.practicum.model.Address;
import ru.yandex.practicum.model.Booking;
import ru.yandex.practicum.model.Warehouse;
import ru.yandex.practicum.repository.BookingRepository;
import ru.yandex.practicum.repository.WarehouseRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final ShoppingStoreClient shoppingStoreClient;
    private final BookingMapper bookingMapper;
    private final BookingRepository bookingRepository;
    private final WarehouseMapper warehouseMapper;

    @Override
    @Transactional
    public void addProduct(NewProductInWarehouseRequest requestDto) {
        UUID productId = requestDto.getProductId();

        if (warehouseRepository.existsById(productId)) {
            throw new SpecifiedProductAlreadyInWarehouseException(
                    String.format("Товар с ID = %s уже заведен на склад", productId));
        }

        Warehouse product = warehouseMapper.toWarehouse(requestDto);
        product.setQuantity(0);
        warehouseRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public BookedProductsDto checkProductQuantity(ShoppingCartDto cartDto) {
        double totalWeight = 0.0;
        double totalVolume = 0.0;
        boolean hasFragile = false;

        for (Map.Entry<UUID, Long> entry : cartDto.getProducts().entrySet()) {
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
    public void shippedToDelivery(ShippedToDeliveryRequest deliveryRequest) {
        Booking booking = bookingRepository.findByOrderId(deliveryRequest.getOrderId()).orElseThrow(
                () -> new NoSpecifiedProductInWarehouseException("Нет информации о товаре на складе."));
        booking.setDeliveryId(deliveryRequest.getDeliveryId());
    }

    @Override
    public void acceptReturn(Map<UUID, Long> products) {
        List<Warehouse> warehousesItems = warehouseRepository.findAllById(products.keySet());
        for (Warehouse warehouse : warehousesItems) {
            warehouse.setQuantity((int) (warehouse.getQuantity() + products.get(warehouse.getProductId())));
        }
    }

    @Override
    public BookedProductsDto assemblyProductsForOrder(AssemblyProductsForOrderRequest assemblyProductsForOrder) {
        Booking booking = bookingRepository.findById(assemblyProductsForOrder.getShoppingCartId()).orElseThrow(
                () -> new RuntimeException(String.format("Shopping cart %s not found", assemblyProductsForOrder.getShoppingCartId()))
        );

        Map<UUID, Long> productsInBooking = booking.getProducts();
        List<Warehouse> productsInWarehouse = warehouseRepository.findAllById(productsInBooking.keySet());
        productsInWarehouse.forEach(warehouse -> {
            if (warehouse.getQuantity() < productsInBooking.get(warehouse.getProductId())) {
                throw new ProductInShoppingCartLowQuantityInWarehouseException("Ошибка, товар из корзины не находится в требуемом количестве на складе.");
            }
        });
        for (Warehouse warehouse : productsInWarehouse) {
            warehouse.setQuantity((int) (warehouse.getQuantity() - productsInBooking.get(warehouse.getProductId())));
        }
        booking.setOrderId(assemblyProductsForOrder.getOrderId());
        return bookingMapper.toBookedProductsDto(booking);
    }

    public BookedProductsDto bookingProducts(ShoppingCartDto shoppingCartDto) {
        Map<UUID, Long> products = shoppingCartDto.getProducts();
        List<Warehouse> productsInWarehouse = warehouseRepository.findAllById(products.keySet());
        productsInWarehouse.forEach(warehouse -> {
            if (warehouse.getQuantity() < products.get(warehouse.getProductId())) {
                throw new ProductInShoppingCartLowQuantityInWarehouseException(
                        "Товар " + warehouse.getProductId() + " is sold out");
            }
        });

        double deliveryVolume = productsInWarehouse.stream()
                .map(v -> v.getParametersDto().getDepth() * v.getParametersDto().getWidth()
                        * v.getParametersDto().getHeight())
                .mapToDouble(Double::doubleValue)
                .sum();

        double deliveryWeight = productsInWarehouse.stream()
                .map(Warehouse::getWeight)
                .mapToDouble(Double::doubleValue)
                .sum();

        boolean fragile = productsInWarehouse.stream()
                .anyMatch(Warehouse::getFragile);

        Booking newBooking = Booking.builder()
                .shoppingCartId(shoppingCartDto.getShoppingCartId())
                .deliveryVolume(deliveryVolume)
                .deliveryWeight(deliveryWeight)
                .fragile(fragile)
                .products(products)
                .build();
        Booking booking = bookingRepository.save(newBooking);
        return bookingMapper.toBookedProductsDto(booking);
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