package conjunta.notification_dispatcher.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SensorInactiveAlert {
    private String alertId;
    private String type;
    private String sensorId;
    private LocalDateTime lastActivity;
    private LocalDateTime timestamp;
    private String severity;
}