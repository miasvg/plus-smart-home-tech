package ru.practicum;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.serverfactory.GrpcServerFactory;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

@SpringBootApplication
@Slf4j
public class CollectorMain {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(CollectorMain.class, args);

        // Проверка что gRPC бины созданы
        checkGrpcBeans(context);

        // Вечное ожидание
        keepAlive();
    }

    private static void checkGrpcBeans(ApplicationContext context) {
        try {
            String[] grpcServerBeans = context.getBeanNamesForType(GrpcServerFactory.class);
            log.info("Found gRPC server beans: {}", Arrays.toString(grpcServerBeans));

            String[] grpcServiceBeans = context.getBeanNamesForAnnotation(GrpcService.class);
            log.info("Found gRPC service beans: {}", Arrays.toString(grpcServiceBeans));

        } catch (Exception e) {
            log.error("Error checking gRPC beans", e);
        }
    }

    private static void keepAlive() {
        CountDownLatch latch = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(latch::countDown));

        try {
            latch.await();
            log.info("Shutting down gracefully");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
