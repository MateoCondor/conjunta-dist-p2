package conjunta.environmental_analyzer.service;

import conjunta.environmental_analyzer.dto.HighTemperatureAlert;
import conjunta.environmental_analyzer.dto.NewSensorReadingEvent;
import conjunta.environmental_analyzer.entity.SensorAnalysis;
import conjunta.environmental_analyzer.repository.SensorAnalysisRepository;
import conjunta.environmental_analyzer.dto.DailyReportGenerated;
import conjunta.environmental_analyzer.dto.SensorInactiveAlert;
import conjunta.environmental_analyzer.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnvironmentalAnalysisService {
    
    private final SensorAnalysisRepository repository;
    private final RabbitTemplate rabbitTemplate;
    private static final double HIGH_TEMPERATURE_THRESHOLD = 40.0;
    private int alertsGenerated = 0;
    
    @RabbitListener(queues = RabbitMQConfig.ENVIRONMENTAL_QUEUE)
    public void processSensorReading(NewSensorReadingEvent event) {
        log.info("Processing sensor reading: {}", event);
        
        // Guardar para análisis
        SensorAnalysis analysis = new SensorAnalysis();
        analysis.setSensorId(event.getSensorId());
        analysis.setType(event.getType());
        analysis.setValue(event.getValue());
        analysis.setTimestamp(event.getTimestamp());
        analysis.setLastActivity(LocalDateTime.now());
        
        repository.save(analysis);
        
        // Analizar temperatura alta
        if ("temperature".equals(event.getType()) && event.getValue() > HIGH_TEMPERATURE_THRESHOLD) {
            HighTemperatureAlert alert = new HighTemperatureAlert(
                    "ALT-" + UUID.randomUUID().toString().substring(0, 8),
                    "HighTemperatureAlert",
                    event.getSensorId(),
                    event.getValue(),
                    HIGH_TEMPERATURE_THRESHOLD,
                    LocalDateTime.now(),
                    "CRITICAL"
            );
            
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ALERT_EXCHANGE,
                    RabbitMQConfig.HIGH_TEMP_ROUTING_KEY,
                    alert
            );
            
            alertsGenerated++;
            log.warn("High temperature alert sent: {}", alert);
        }
    }
    
    public List<SensorAnalysis> getSensorAnalysis(String sensorId) {
        return repository.findBySensorIdOrderByTimestampDesc(sensorId);
    }
    
    public List<SensorAnalysis> getRecentAnalysis(String type, int hours) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(hours);
        if (type != null && !type.isEmpty()) {
            return repository.findByTypeAndTimestampAfterOrderByTimestampDesc(type, cutoff);
        }
        return repository.findByTimestampAfterOrderByTimestampDesc(cutoff);
    }
    
    public List<String> getInactiveSensors(int hours) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(hours);
        return repository.findInactiveSensors(cutoff);
    }
    
    public Map<String, Object> getSensorStatistics(String sensorId, int hours) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(hours);
        List<SensorAnalysis> readings = repository.findBySensorIdAndTimestampAfter(sensorId, cutoff);
        
        Map<String, Object> statistics = new HashMap<>();
        
        if (readings.isEmpty()) {
            statistics.put("sensorId", sensorId);
            statistics.put("count", 0);
            statistics.put("message", "No hay datos en el período especificado");
            return statistics;
        }
        
        DoubleSummaryStatistics stats = readings.stream()
                .mapToDouble(SensorAnalysis::getValue)
                .summaryStatistics();
        
        statistics.put("sensorId", sensorId);
        statistics.put("count", readings.size());
        statistics.put("average", Math.round(stats.getAverage() * 100.0) / 100.0);
        statistics.put("min", stats.getMin());
        statistics.put("max", stats.getMax());
        statistics.put("lastReading", readings.get(0).getTimestamp());
        statistics.put("type", readings.get(0).getType());
        
        return statistics;
    }
    
    public Map<String, Object> getDailyReport(String dateStr) {
        LocalDate date = dateStr != null ? 
                LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE) : 
                LocalDate.now().minusDays(1);
        
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        
        List<SensorAnalysis> dayReadings = repository.findByTimestampBetween(startOfDay, endOfDay);
        
        Map<String, Object> report = new HashMap<>();
        report.put("date", date.toString());
        report.put("totalReadings", dayReadings.size());
        
        // Agrupar por tipo de sensor
        Map<String, List<Double>> valuesByType = new HashMap<>();
        Map<String, Set<String>> sensorsByType = new HashMap<>();
        
        for (SensorAnalysis reading : dayReadings) {
            valuesByType.computeIfAbsent(reading.getType(), k -> new ArrayList<>()).add(reading.getValue());
            sensorsByType.computeIfAbsent(reading.getType(), k -> new HashSet<>()).add(reading.getSensorId());
        }
        
        Map<String, Map<String, Object>> typeStatistics = new HashMap<>();
        for (String type : valuesByType.keySet()) {
            List<Double> values = valuesByType.get(type);
            DoubleSummaryStatistics stats = values.stream().mapToDouble(Double::doubleValue).summaryStatistics();
            
            Map<String, Object> typeStat = new HashMap<>();
            typeStat.put("sensorCount", sensorsByType.get(type).size());
            typeStat.put("readingCount", values.size());
            typeStat.put("average", Math.round(stats.getAverage() * 100.0) / 100.0);
            typeStat.put("min", stats.getMin());
            typeStat.put("max", stats.getMax());
            
            typeStatistics.put(type, typeStat);
        }
        
        report.put("statisticsByType", typeStatistics);
        return report;
    }
    
    public Map<String, Long> getAlertsCount(int hours) {
        Map<String, Long> alertsCount = new HashMap<>();
        alertsCount.put("alertsGenerated", (long) alertsGenerated);
        alertsCount.put("period", (long) hours);
        alertsCount.put("timestamp", System.currentTimeMillis());
        return alertsCount;
    }
    
    @Scheduled(fixedRate = 21600000) // 6 horas
    public void checkInactiveSensors() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        List<String> inactiveSensors = repository.findInactiveSensors(cutoff);
        
        for (String sensorId : inactiveSensors) {
            log.warn("Sensor {} is inactive for more than 24 hours", sensorId);
            
            SensorInactiveAlert alert = new SensorInactiveAlert(
                "INA-" + UUID.randomUUID().toString().substring(0, 8),
                "SensorInactiveAlert",
                sensorId,
                cutoff,
                LocalDateTime.now(),
                "WARNING"
            );
            
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.ALERT_EXCHANGE,
                RabbitMQConfig.SENSOR_INACTIVE_ROUTING_KEY,
                alert
            );
            
            log.info("Inactive sensor alert sent: {}", alert);
        }
    }
    
    @Scheduled(cron = "0 0 0 * * *") // Cada día a medianoche
    public void generateDailyReport() {
        log.info("Generating daily report...");
        Map<String, Object> reportData = getDailyReport(null);
        
        DailyReportGenerated report = new DailyReportGenerated(
            "RPT-" + UUID.randomUUID().toString().substring(0, 8),
            "DailyReportGenerated",
            LocalDate.now().minusDays(1),
            reportData,
            (Long) reportData.get("totalReadings")
        );
        
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.ALERT_EXCHANGE,
            "report.daily.generated",
            report
        );
        
        log.info("Daily report generated and sent: {}", report);
    }
}