package ru.practicum.dto.sensors;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Абстрактный базовый класс для всех событий от датчиков.
 * Содержит общие поля и аннотации для полиморфной десериализации JSON.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ClimateSensorEvent.class, name = "CLIMATE_SENSOR_EVENT"),
        @JsonSubTypes.Type(value = LightSensorEvent.class, name = "LIGHT_SENSOR_EVENT"),
        @JsonSubTypes.Type(value = MotionSensorEvent.class, name = "MOTION_SENSOR_EVENT"),
        @JsonSubTypes.Type(value = SwitchSensorEvent.class, name = "SWITCH_SENSOR_EVENT"),
        @JsonSubTypes.Type(value = TemperatureSensorEvent.class, name = "TEMPERATURE_SENSOR_EVENT")
})
@Getter
@Setter
@ToString
@Component
public abstract class SensorEvent {

    @NotBlank(message = "Идентификатор события не может быть пустым")
    private String id;

    @NotBlank(message = "Идентификатор хаба не может быть пустым")
    private String hubId;

    private Instant timestamp = Instant.now();

    @NotNull(message = "Тип события не может быть null")
    public abstract SensorEventType getType();
}
