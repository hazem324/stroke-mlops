package tn.esprit.test.stroke_backend.dto.study;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import tn.esprit.test.stroke_backend.entities.Modality;
import tn.esprit.test.stroke_backend.entities.StudiesStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StudyResponseDTO {

    Long id;

    String studyCode;

    LocalDate studyDate;

    Modality modality;

    StudiesStatus status;

    Long patientId;

    String patientCode;

    String patientFullName;

    String dwiFileName;

    Long dwiFileSize;

    PredictionResponseDTO prediction;

    String errorMessage;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;
}