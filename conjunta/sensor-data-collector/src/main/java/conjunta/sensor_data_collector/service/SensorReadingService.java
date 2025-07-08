package conjunta.sensor_data_collector.service;

import conjunta.sensor_data_collector.dto.NewSensorReadingEvent;
import conjunta.sensor_data_collector.dto.SensorReadingRequest;
import conjunta.sensor_data_collector.entity.SensorReading;
import conjunta.sensor_data_collector.repository.SensorReadingRepository;
import conjunta.sensor_data_collector.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SensorReadingService {
    
    private final SensorReadingRepository repository;
    private final RabbitTemplate rabbitTemplate;
    
    public SensorReading saveSensorReading(SensorReadingRequest request) {
        SensorReading sensorReading = new SensorReading();
        sensorReading.setSensorId(request.getSensorId());
        sensorReading.setType(request.getType());
        sensorReading.setValue(request.getValue());
        sensorReading.setTimestamp(request.getTimestamp() != null ? request.getTimestamp() : LocalDateTime.now());
        
        SensorReading saved = repository.save(sensorReading);
        
        // Emitir evento
        NewSensorReadingEvent event = new NewSensorReadingEvent(
                "EVT-" + UUID.randomUUID().toString().substring(0, 8),
                saved.getSensorId(),
                saved.getType(),
                saved.getValue(),
                saved.getTimestamp()
        );
        
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.SENSOR_READING_EXCHANGE,
                RabbitMQConfig.SENSOR_READING_ROUTING_KEY,
                event
        );
        
        log.info("Sensor reading saved and event emitted: {}", event);
        return saved;
    }
    
    public List<SensorReading> getSensorHistory(String sensorId) {
        return repository.findBySensorIdOrderByTimestampDesc(sensorId);
    }
    
    public List<String> getAllSensors() {
        return repository.findDistinctSensorIds();
    }
}