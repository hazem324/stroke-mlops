package tn.esprit.test.stroke_backend.dto.statistics;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecentAnalysisDTO {

    Long studyId;
    Long patientId;
    String patientCode;
    String patientName;
    String studyCode;
    LocalDate studyDate;
    String modality;
    String status;
    Boolean lesionDetected;
    private LocalDateTime createdAt;
}