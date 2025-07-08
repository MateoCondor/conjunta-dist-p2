package conjunta.environmental_analyzer.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewSensorReadingEvent {
    private String eventId;
    private String sensorId;
    private String type;
    private Double value;
    private LocalDateTime timestamp;
}