package ru.practicum.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.practicum.mapper.HubAndSensorMapper;
import ru.practicum.service.TelemetryService;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;


@GrpcService
@RequiredArgsConstructor
@Slf4j
@Service
public class CollectorGrpcService extends CollectorControllerGrpc.CollectorControllerImplBase {
    private final TelemetryService telemetryService;
    private final HubAndSensorMapper mapper;

    @Override
    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            log.info("Получен gRPC запрос на collectSensorEvent: {}", request);
            SensorEventAvro event = mapper.toAvro(request);
            telemetryService.send(event);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Ошибка обработки SensorEvent", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            log.info("Получен gRPC запрос на collectHubEvent: {}", request);
            HubEventAvro event = mapper.toAvro(request);
            telemetryService.send(event);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Ошибка обработки HubEvent", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }
}