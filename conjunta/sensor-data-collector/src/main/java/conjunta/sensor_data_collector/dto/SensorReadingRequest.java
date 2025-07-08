package conjunta.sensor_data_collector.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SensorReadingRequest {
    private String sensorId;
    private String type;
    private Double value;
    private LocalDateTime timestamp;
}