package ru.practicum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.CountDownLatch;

@SpringBootApplication
@Slf4j
public class CollectorMain {

    public static void main(String[] args) {
        SpringApplication.run(CollectorMain.class, args);
    }

    @Bean
    public ApplicationRunner keepAliveRunner() {
        return args -> {
            log.info("✅ Collector application started successfully");
            log.info("✅ gRPC server should be listening on port 59091");
            CountDownLatch latch = new CountDownLatch(1);
            Runtime.getRuntime().addShutdownHook(new Thread(latch::countDown));

            try {
                latch.await();
                log.info("✅ Collector application stopped gracefully");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Main thread interrupted", e);
            }
        };
    }
}
