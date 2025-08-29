package ru.yandex.practicum.service;


import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import ru.practicum.telemetry.message.ActionTypeProto;
import ru.practicum.telemetry.message.DeviceActionProto;
import ru.practicum.telemetry.message.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;


import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class HubRouterClientService {

    private final HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterStub;

    /**
     * Отправляет команду устройства через Hub Router
     */
    @Retryable(
            value = {StatusRuntimeException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void sendDeviceCommand(String hubId, String scenarioName,
                                  String sensorId, String actionType, Integer value) {
        try {
            DeviceActionRequest request = buildDeviceActionRequest(
                    hubId, scenarioName, sensorId, actionType, value
            );

            hubRouterStub.withDeadlineAfter(5, TimeUnit.SECONDS)
                    .handleDeviceAction(request);

            log.info("✅ Command sent successfully: hub={}, scenario={}, sensor={}, action={}, value={}",
                    hubId, scenarioName, sensorId, actionType, value);

        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.DEADLINE_EXCEEDED) {
                log.error("⏰ Timeout sending command to Hub Router for sensor: {}", sensorId);
            } else {
                log.error("❌ gRPC error sending command for sensor: {}", sensorId, e);
            }
            throw e;
        } catch (Exception e) {
            log.error("❌ Unexpected error sending command for sensor: {}", sensorId, e);
            throw new RuntimeException("Failed to send device command", e);
        }
    }

    /**
     * Строит gRPC запрос для отправки команды
     */
    private DeviceActionRequest buildDeviceActionRequest(String hubId, String scenarioName,
                                                         String sensorId, String actionType, Integer value) {
        DeviceActionProto actionProto = DeviceActionProto.newBuilder()
                .setSensorId(sensorId)
                .setType(ActionTypeProto.valueOf(actionType))
                .setValue(value != null ? value : 0)
                .build();

        return DeviceActionRequest.newBuilder()
                .setHubId(hubId)
                .setScenarioName(scenarioName)
                .setAction(actionProto)
                .setTimestamp(com.google.protobuf.Timestamp.newBuilder()
                        .setSeconds(Instant.now().getEpochSecond())
                        .setNanos(Instant.now().getNano()))
                .build();
    }

    /**
     * Проверяет доступность Hub Router
     */
    public boolean isHubRouterAvailable() {
        try {
            // Простой ping-запрос для проверки доступности
            hubRouterStub.withDeadlineAfter(2, TimeUnit.SECONDS)
                    .handleDeviceAction(DeviceActionRequest.getDefaultInstance());
            return true;
        } catch (Exception e) {
            log.warn("Hub Router is not available: {}", e.getMessage());
            return false;
        }
    }
}
