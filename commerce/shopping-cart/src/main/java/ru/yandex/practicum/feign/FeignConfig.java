package ru.yandex.practicum.feign;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(clients = WarehouseClient.class)
public class FeignConfig {
}

