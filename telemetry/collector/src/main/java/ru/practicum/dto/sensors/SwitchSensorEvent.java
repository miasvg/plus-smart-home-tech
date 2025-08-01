package ru.practicum.dto.sensors;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Событие датчика переключателя, содержащее информацию о текущем состоянии переключателя.
 */
@Getter
@Setter
@ToString(callSuper = true)
public class SwitchSensorEvent extends SensorEvent {

    @NotNull(message = "Состояние переключателя не может быть null")
    private Boolean state;

    @Override
    public SensorEventType getType() {
        return SensorEventType.SWITCH_SENSOR_EVENT;
    }
}

