package conjunta.notification_dispatcher.controller;

import conjunta.notification_dispatcher.entity.Notification;
import conjunta.notification_dispatcher.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    
    private final NotificationService service;
    
    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications(
            @RequestParam(required = false, defaultValue = "100") int limit) {
        List<Notification> notifications = service.getAllNotifications(limit);
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/pending")
    public ResponseEntity<List<Notification>> getPendingNotifications() {
        List<Notification> pending = service.getPendingNotifications();
        return ResponseEntity.ok(pending);
    }
    
    @GetMapping("/sent")
    public ResponseEntity<List<Notification>> getSentNotifications(
            @RequestParam(required = false, defaultValue = "100") int limit) {
        List<Notification> sent = service.getSentNotifications(limit);
        return ResponseEntity.ok(sent);
    }
    
    @GetMapping("/by-severity/{severity}")
    public ResponseEntity<List<Notification>> getNotificationsBySeverity(
            @PathVariable String severity,
            @RequestParam(required = false, defaultValue = "100") int limit) {
        List<Notification> notifications = service.getNotificationsBySeverity(severity, limit);
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/by-alert/{alertId}")
    public ResponseEntity<Notification> getNotificationByAlertId(@PathVariable String alertId) {
        Notification notification = service.getNotificationByAlertId(alertId);
        if (notification != null) {
            return ResponseEntity.ok(notification);
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getNotificationStatistics(
            @RequestParam(required = false, defaultValue = "24") int hours) {
        Map<String, Object> statistics = service.getNotificationStatistics(hours);
        return ResponseEntity.ok(statistics);
    }
    
    @PostMapping("/resend/{id}")
    public ResponseEntity<String> resendNotification(@PathVariable Long id) {
        boolean resent = service.resendNotification(id);
        if (resent) {
            return ResponseEntity.ok("Notificación reenviada exitosamente");
        }
        return ResponseEntity.badRequest().body("No se pudo reenviar la notificación");
    }
    
    @PostMapping("/mark-sent/{id}")
    public ResponseEntity<String> markAsSent(@PathVariable Long id) {
        boolean marked = service.markAsSent(id);
        if (marked) {
            return ResponseEntity.ok("Notificación marcada como enviada");
        }
        return ResponseEntity.badRequest().body("No se pudo marcar la notificación");
    }
    
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getNotificationSummary() {
        Map<String, Object> summary = service.getNotificationSummary();
        return ResponseEntity.ok(summary);
    }
}