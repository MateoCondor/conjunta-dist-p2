package conjunta.sensor_data_collector.controller;

import conjunta.sensor_data_collector.dto.SensorReadingRequest;
import conjunta.sensor_data_collector.entity.SensorReading;
import conjunta.sensor_data_collector.service.SensorReadingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/sensor-readings")
@RequiredArgsConstructor
public class SensorReadingController {
    
    private final SensorReadingService service;
    
    @PostMapping
    public ResponseEntity<SensorReading> createSensorReading(@RequestBody SensorReadingRequest request) {
        SensorReading saved = service.saveSensorReading(request);
        return ResponseEntity.ok(saved);
    }
    
    @GetMapping("/{sensorId}")
    public ResponseEntity<List<SensorReading>> getSensorHistory(@PathVariable String sensorId) {
        List<SensorReading> history = service.getSensorHistory(sensorId);
        return ResponseEntity.ok(history);
    }
    
    @GetMapping("/sensors")
    public ResponseEntity<List<String>> getAllSensors() {
        List<String> sensors = service.getAllSensors();
        return ResponseEntity.ok(sensors);
    }
}