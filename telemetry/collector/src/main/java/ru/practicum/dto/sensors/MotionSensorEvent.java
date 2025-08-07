package ru.practicum.dto.sensors;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Событие датчика движения.
 */
@Getter
@Setter
@ToString(callSuper = true)
public class MotionSensorEvent extends SensorEvent {

    @NotNull(message = "Качество связи не может быть null")
    private Integer linkQuality;

    @NotNull(message = "Наличие движения не может быть null")
    private Boolean motion;

    @NotNull(message = "Напряжение не может быть null")
    private Integer voltage;

    @Override
    public SensorEventType getType() {
        return SensorEventType.MOTION_SENSOR_EVENT;
    }
}
