package conjunta.environmental_analyzer.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HighTemperatureAlert {
    private String alertId;
    private String type;
    private String sensorId;
    private Double value;
    private Double threshold;
    private LocalDateTime timestamp;
    private String severity;
}