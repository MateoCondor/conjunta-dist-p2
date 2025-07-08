package conjunta.sensor_data_collector.repository;

import conjunta.sensor_data_collector.entity.SensorReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {
    List<SensorReading> findBySensorIdOrderByTimestampDesc(String sensorId);
    
    @Query("SELECT DISTINCT s.sensorId FROM SensorReading s ORDER BY s.sensorId")
    List<String> findDistinctSensorIds();
}