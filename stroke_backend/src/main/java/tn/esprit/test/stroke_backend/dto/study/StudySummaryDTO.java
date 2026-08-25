package tn.esprit.test.stroke_backend.dto.study;

import java.time.LocalDate;

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
public class StudySummaryDTO {

    Long id;

    String studyCode;

    LocalDate studyDate;

    Modality modality;

    StudiesStatus status;
}