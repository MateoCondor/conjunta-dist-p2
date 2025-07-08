package conjunta.sensor_data_collector.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_readings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SensorReading {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String sensorId;
    
    @Column(nullable = false)
    private String type;
    
    @Column(nullable = false)
    private Double value;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
}