package conjunta.environmental_analyzer.controller;

import conjunta.environmental_analyzer.entity.SensorAnalysis;
import conjunta.environmental_analyzer.service.EnvironmentalAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/environmental")
@RequiredArgsConstructor
public class EnvironmentalController {
    
    private final EnvironmentalAnalysisService service;
    
    @GetMapping("/analysis/{sensorId}")
    public ResponseEntity<List<SensorAnalysis>> getSensorAnalysis(@PathVariable String sensorId) {
        List<SensorAnalysis> analysis = service.getSensorAnalysis(sensorId);
        return ResponseEntity.ok(analysis);
    }
    
    @GetMapping("/analysis")
    public ResponseEntity<List<SensorAnalysis>> getAllAnalysis(
            @RequestParam(required = false) String type,
            @RequestParam(required = false, defaultValue = "24") int hours) {
        List<SensorAnalysis> analysis = service.getRecentAnalysis(type, hours);
        return ResponseEntity.ok(analysis);
    }
    
    @GetMapping("/inactive-sensors")
    public ResponseEntity<List<String>> getInactiveSensors(
            @RequestParam(required = false, defaultValue = "24") int hours) {
        List<String> inactiveSensors = service.getInactiveSensors(hours);
        return ResponseEntity.ok(inactiveSensors);
    }
    
    @GetMapping("/statistics/{sensorId}")
    public ResponseEntity<Map<String, Object>> getSensorStatistics(
            @PathVariable String sensorId,
            @RequestParam(required = false, defaultValue = "24") int hours) {
        Map<String, Object> statistics = service.getSensorStatistics(sensorId, hours);
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/daily-report")
    public ResponseEntity<Map<String, Object>> getDailyReport(
            @RequestParam(required = false) String date) {
        Map<String, Object> report = service.getDailyReport(date);
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/alerts/count")
    public ResponseEntity<Map<String, Long>> getAlertsCount(
            @RequestParam(required = false, defaultValue = "24") int hours) {
        Map<String, Long> alertsCount = service.getAlertsCount(hours);
        return ResponseEntity.ok(alertsCount);
    }
}