package conjunta.environmental_analyzer.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SensorInactiveAlert {
    private String alertId;
    private String type = "SensorInactiveAlert";
    private String sensorId;
    private LocalDateTime lastActivity;
    private LocalDateTime timestamp;
    private String severity = "WARNING";
}