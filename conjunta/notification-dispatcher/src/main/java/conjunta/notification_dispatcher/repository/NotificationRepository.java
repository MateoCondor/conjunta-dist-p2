package conjunta.notification_dispatcher.repository;

import conjunta.notification_dispatcher.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findBySentFalseAndSeverityNot(String severity);
    List<Notification> findBySentFalse();
    
    // Nuevos métodos para los controladores
    List<Notification> findAllByOrderByTimestampDesc(Pageable pageable);
    List<Notification> findBySentFalseOrderByTimestampDesc();
    List<Notification> findBySentTrueOrderByTimestampDesc(Pageable pageable);
    List<Notification> findBySeverityOrderByTimestampDesc(String severity, Pageable pageable);
    
    Notification findByAlertId(String alertId);
    
    // Métodos de conteo para estadísticas
    long countByTimestampAfter(LocalDateTime timestamp);
    long countBySentTrueAndTimestampAfter(LocalDateTime timestamp);
    long countBySentFalseAndTimestampAfter(LocalDateTime timestamp);
    long countBySeverityAndTimestampAfter(String severity, LocalDateTime timestamp);
    long countBySentTrue();
    long countBySentFalse();
    long countBySeverity(String severity);
}