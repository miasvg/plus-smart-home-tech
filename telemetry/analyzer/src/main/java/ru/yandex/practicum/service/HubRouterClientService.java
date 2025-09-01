package ru.yandex.practicum.service;

import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.practicum.telemetry.message.ActionTypeProto;
import ru.practicum.telemetry.message.DeviceActionProto;
import ru.practicum.telemetry.message.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;
import ru.yandex.practicum.jpa_entities.Action;
import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;
import java.time.Instant;

@Slf4j
@Service
public class HubRouterClientService {

    @GrpcClient("hub-router")
    private final HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient;

    public HubRouterClientService(
                           HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient) {
        this.hubRouterClient = hubRouterClient;
    }


    public void sendAction(Action action) {
        DeviceActionRequest deviceActionRequest = buildActionRequest(action);
        hubRouterClient.handleDeviceAction(deviceActionRequest);
        log.info("Действие {} отправлено в hub-router", deviceActionRequest);
    }

    private DeviceActionRequest buildActionRequest(Action action) {
        // Определяем значение для действия
        Integer value = action.getValue();
        switch (action.getType()) {
            case SET_VALUE -> {
                if (value == null) {
                    throw new IllegalStateException("SET_VALUE action must have a non-null value");
                }
            }
            case ACTIVATE, DEACTIVATE, INVERSE -> {
                if (value == null) {
                    value = 0;
                }
            }
            default -> throw new IllegalArgumentException("Unsupported action type: " + action.getType());
        }

        DeviceActionProto deviceActionProto = DeviceActionProto.newBuilder()
                .setSensorId(action.getSensor().getId())
                .setType(actionTypeProto(action.getType()))
                .setValue(value)
                .build();

        return DeviceActionRequest.newBuilder()
                .setHubId(action.getScenario().getHubId())
                .setScenarioName(action.getScenario().getName())
                .setAction(deviceActionProto)
                .setTimestamp(setTimestamp())
                .build();
    }


    private ActionTypeProto actionTypeProto(ActionTypeAvro actionType) {
        return switch (actionType) {
            case ACTIVATE -> ActionTypeProto.ACTIVATE;
            case DEACTIVATE -> ActionTypeProto.DEACTIVATE;
            case INVERSE -> ActionTypeProto.INVERSE;
            case SET_VALUE -> ActionTypeProto.SET_VALUE;
        };
    }

    private Timestamp setTimestamp() {
        Instant instant = Instant.now();
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }
}
