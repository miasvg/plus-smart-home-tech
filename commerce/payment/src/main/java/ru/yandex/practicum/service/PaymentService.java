package ru.yandex.practicum.service;


import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.PaymentDto;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentService {

    PaymentDto createPayment(OrderDto orderDto);

    Double getTotalCost(OrderDto orderDto);

    void paymentSuccess(UUID uuid);

    BigDecimal productCost(OrderDto orderDto);

    void paymentFailed(UUID uuid);
}
