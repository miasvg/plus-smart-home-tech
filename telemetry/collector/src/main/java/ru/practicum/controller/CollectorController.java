package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.service.TelemetryService;
import ru.practicum.dto.sensors.SensorEvent;
import ru.practicum.dto.hubs.HubEvent;


@RestController
@RequestMapping("/events") // Используем путь из OpenAPI спецификации
@RequiredArgsConstructor
@Slf4j
public class CollectorController {

    private final TelemetryService telemetryService;

    @PostMapping("/sensors")
    public ResponseEntity<Void> collectSensorEvent(@Valid @RequestBody SensorEvent event) {
        log.info("Получен POST-запрос на /events/sensors с телом: {}", event);
        telemetryService.send(event);
        return ResponseEntity.ok().build();
    }

    /**
     * Эндпоинт для обработки событий от хаба.
     * @param event DTO события, которое будет автоматически десериализовано
     * в нужный подтип.
     * @return HTTP 200 OK в случае успешной обработки.
     */
    @PostMapping("/hubs")
    public ResponseEntity<Void> collectHubEvent(@Valid @RequestBody HubEvent event) {
        log.info("Получен POST-запрос на /events/hubs с телом: {}", event);
        telemetryService.send(event);
        return ResponseEntity.ok().build();
    }
}