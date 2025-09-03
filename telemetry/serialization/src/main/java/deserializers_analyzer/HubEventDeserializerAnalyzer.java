package deserializers_analyzer;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

@Component
public class HubEventDeserializerAnalyzer extends BaseAvroDeserializer<HubEventAvro> {

    public HubEventDeserializerAnalyzer() {
        super(HubEventAvro.getClassSchema());
    }
}