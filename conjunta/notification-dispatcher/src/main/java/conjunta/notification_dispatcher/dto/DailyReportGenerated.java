package conjunta.notification_dispatcher.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailyReportGenerated {
    private String reportId;
    private String type;
    private LocalDate date;
    private Map<String, Object> reportData;
    private Long totalReadings;
}