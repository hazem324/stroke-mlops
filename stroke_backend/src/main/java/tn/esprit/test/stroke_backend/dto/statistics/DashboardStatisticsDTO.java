package tn.esprit.test.stroke_backend.dto.statistics;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DashboardStatisticsDTO {

    long totalPatients;
    long totalStudies;
    long completedAnalyses;
    long pendingAnalyses;
}