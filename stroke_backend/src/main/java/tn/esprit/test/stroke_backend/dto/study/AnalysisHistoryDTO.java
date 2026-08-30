package tn.esprit.test.stroke_backend.dto.study;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AnalysisHistoryDTO  {

    Long studyId;
    Long patientId;
    String patientCode;
    String patientName;
    String studyCode;
    LocalDate studyDate;
    String modality;
    String status;
    Boolean lesionDetected;
    LocalDateTime createdAt;
}