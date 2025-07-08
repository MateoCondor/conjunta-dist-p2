package conjunta.notification_dispatcher.service;

import conjunta.notification_dispatcher.dto.HighTemperatureAlert;
import conjunta.notification_dispatcher.entity.Notification;
import conjunta.notification_dispatcher.repository.NotificationRepository;
import conjunta.notification_dispatcher.dto.SensorInactiveAlert;
import conjunta.notification_dispatcher.dto.DailyReportGenerated;
import conjunta.notification_dispatcher.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final NotificationRepository repository;
    
    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void processAlert(HighTemperatureAlert alert) {
        log.info("Processing alert: {}", alert);
        
        String message = String.format(
                "ALERTA: Temperatura alta detectada en sensor %s. Valor: %.2f°C (Umbral: %.2f°C)",
                alert.getSensorId(), alert.getValue(), alert.getThreshold()
        );
        
        Notification notification = new Notification();
        notification.setAlertId(alert.getAlertId());
        notification.setType(alert.getType());
        notification.setMessage(message);
        notification.setSeverity(alert.getSeverity());
        notification.setTimestamp(LocalDateTime.now());
        notification.setSent(false);
        
        repository.save(notification);
        
        // Enviar notificación según prioridad
        if ("CRITICAL".equals(alert.getSeverity())) {
            sendImmediateNotification(notification);
        }
    }
    
    public List<Notification> getAllNotifications(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return repository.findAllByOrderByTimestampDesc(pageable);
    }
    
    public List<Notification> getPendingNotifications() {
        return repository.findBySentFalseOrderByTimestampDesc();
    }
    
    public List<Notification> getSentNotifications(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return repository.findBySentTrueOrderByTimestampDesc(pageable);
    }
    
    public List<Notification> getNotificationsBySeverity(String severity, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return repository.findBySeverityOrderByTimestampDesc(severity, pageable);
    }
    
    public Notification getNotificationByAlertId(String alertId) {
        return repository.findByAlertId(alertId);
    }
    
    public Map<String, Object> getNotificationStatistics(int hours) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(hours);
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("period", hours + " hours");
        statistics.put("total", repository.countByTimestampAfter(cutoff));
        statistics.put("sent", repository.countBySentTrueAndTimestampAfter(cutoff));
        statistics.put("pending", repository.countBySentFalseAndTimestampAfter(cutoff));
        statistics.put("critical", repository.countBySeverityAndTimestampAfter("CRITICAL", cutoff));
        statistics.put("warning", repository.countBySeverityAndTimestampAfter("WARNING", cutoff));
        statistics.put("info", repository.countBySeverityAndTimestampAfter("INFO", cutoff));
        
        return statistics;
    }
    
    public boolean resendNotification(Long id) {
        Optional<Notification> notificationOpt = repository.findById(id);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            sendImmediateNotification(notification);
            return true;
        }
        return false;
    }
    
    public boolean markAsSent(Long id) {
        Optional<Notification> notificationOpt = repository.findById(id);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            notification.setSent(true);
            repository.save(notification);
            return true;
        }
        return false;
    }
    
    public Map<String, Object> getNotificationSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        long total = repository.count();
        long sent = repository.countBySentTrue();
        long pending = repository.countBySentFalse();
        
        summary.put("total", total);
        summary.put("sent", sent);
        summary.put("pending", pending);
        summary.put("sentPercentage", total > 0 ? Math.round(((double) sent / total) * 100) : 0);
        
        // Últimas 24 horas
        LocalDateTime last24h = LocalDateTime.now().minusHours(24);
        summary.put("last24hours", repository.countByTimestampAfter(last24h));
        
        // Por severidad
        Map<String, Long> bySeverity = new HashMap<>();
        bySeverity.put("CRITICAL", repository.countBySeverity("CRITICAL"));
        bySeverity.put("WARNING", repository.countBySeverity("WARNING"));
        bySeverity.put("INFO", repository.countBySeverity("INFO"));
        summary.put("bySeverity", bySeverity);
        
        return summary;
    }
    
    @Async
    public void sendImmediateNotification(Notification notification) {
        sendEmailNotification(notification);
        sendSMSNotification(notification);
        sendPushNotification(notification);
        
        notification.setSent(true);
        notification.setChannel("EMAIL,SMS,PUSH");
        repository.save(notification);
    }
    
    private void sendEmailNotification(Notification notification) {
        log.info("📧 EMAIL enviado: {}", notification.getMessage());
    }
    
    private void sendSMSNotification(Notification notification) {
        log.info("📱 SMS enviado: {}", notification.getMessage());
    }
    
    private void sendPushNotification(Notification notification) {
        log.info("🔔 PUSH notification: {}", notification.getMessage());
    }
    
    @Scheduled(fixedRate = 1800000) // 30 minutos
    public void sendBatchNotifications() {
        List<Notification> pendingNotifications = repository.findBySentFalseAndSeverityNot("CRITICAL");
        
        if (!pendingNotifications.isEmpty()) {
            log.info("Enviando {} notificaciones agrupadas", pendingNotifications.size());
            
            for (Notification notification : pendingNotifications) {
                sendEmailNotification(notification);
                notification.setSent(true);
                notification.setChannel("EMAIL");
                repository.save(notification);
            }
        }
    }

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void processHighTemperatureAlert(HighTemperatureAlert alert) {
        log.info("Processing high temperature alert: {}", alert);
        
        String message = String.format(
                "ALERTA: Temperatura alta detectada en sensor %s. Valor: %.2f°C (Umbral: %.2f°C)",
                alert.getSensorId(), alert.getValue(), alert.getThreshold()
        );
        
        createAndSaveNotification(alert.getAlertId(), alert.getType(), message, alert.getSeverity());
    }
    
    @RabbitListener(queues = "inactive.sensor.queue")
    public void processSensorInactiveAlert(SensorInactiveAlert alert) {
        log.info("Processing sensor inactive alert: {}", alert);
        
        String message = String.format(
                "ALERTA: Sensor %s inactivo desde %s",
                alert.getSensorId(), alert.getLastActivity()
        );
        
        createAndSaveNotification(alert.getAlertId(), alert.getType(), message, alert.getSeverity());
    }
    
    @RabbitListener(queues = "daily.report.queue")
    public void processDailyReport(DailyReportGenerated report) {
        log.info("Processing daily report: {}", report);
        
        String message = String.format(
                "Reporte diario generado para %s. Total de lecturas: %d",
                report.getDate(), report.getTotalReadings()
        );
        
        createAndSaveNotification(report.getReportId(), report.getType(), message, "INFO");
    }
    
    private void createAndSaveNotification(String alertId, String type, String message, String severity) {
        Notification notification = new Notification();
        notification.setAlertId(alertId);
        notification.setType(type);
        notification.setMessage(message);
        notification.setSeverity(severity);
        notification.setTimestamp(LocalDateTime.now());
        notification.setSent(false);
        
        repository.save(notification);
        
        // Enviar notificación según prioridad
        if ("CRITICAL".equals(severity)) {
            sendImmediateNotification(notification);
        }
    }
}