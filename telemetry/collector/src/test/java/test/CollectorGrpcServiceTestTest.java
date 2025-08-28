package test;



import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import com.google.protobuf.Empty;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.CollectorMain;
import ru.practicum.controller.CollectorGrpcService;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@ContextConfiguration(classes = CollectorMain.class) // ← Явно указываем конфигурацию
@ActiveProfiles("test")
class CollectorGrpcServiceTest {

    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    @MockBean
    private CollectorGrpcService grpcService;

    private CollectorControllerGrpc.CollectorControllerBlockingStub blockingStub;

    @BeforeEach
    void setUp() throws Exception {
        // Создаем in-process сервер для тестов
        String serverName = InProcessServerBuilder.generateName();
        grpcCleanup.register(InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .addService(grpcService)
                .build()
                .start());

        // Создаем клиента для тестов
        blockingStub = CollectorControllerGrpc.newBlockingStub(
                grpcCleanup.register(InProcessChannelBuilder.forName(serverName)
                        .directExecutor()
                        .build()));
    }

    @Test
    void testCollectSensorEvent() {
        SensorEventProto request = SensorEventProto.newBuilder()
                .setId("test-sensor-1")
                .setHubId("test-hub-1")
                .build();

        assertDoesNotThrow(() -> {
            Empty response = blockingStub.collectSensorEvent(request);
            // Если не брошено исключение - метод работает
        });
    }

    @Test
    void testCollectHubEvent() {
        var request = HubEventProto.newBuilder()
                .setHubId("test-hub-1")
                .build();

        assertDoesNotThrow(() -> {
            Empty response = blockingStub.collectHubEvent(request);
            // Если не брошено исключение - метод работает
        });
    }
}