package conjunta.environmental_analyzer.repository;

import conjunta.environmental_analyzer.entity.SensorAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SensorAnalysisRepository extends JpaRepository<SensorAnalysis, Long> {
    
    @Query("SELECT DISTINCT s.sensorId FROM SensorAnalysis s WHERE s.lastActivity < :cutoff")
    List<String> findInactiveSensors(@Param("cutoff") LocalDateTime cutoff);
    
    List<SensorAnalysis> findBySensorIdOrderByTimestampDesc(String sensorId);
    
    List<SensorAnalysis> findByTimestampAfterOrderByTimestampDesc(LocalDateTime cutoff);
    
    List<SensorAnalysis> findByTypeAndTimestampAfterOrderByTimestampDesc(String type, LocalDateTime cutoff);
    
    List<SensorAnalysis> findBySensorIdAndTimestampAfter(String sensorId, LocalDateTime cutoff);
    
    List<SensorAnalysis> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
}