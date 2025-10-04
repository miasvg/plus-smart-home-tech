package ru.yandex.practicum.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.feign.*;

@Configuration
@EnableFeignClients(clients = {OrderClient.class, ShoppingStoreClient.class})
public class FeignConfig {
}
