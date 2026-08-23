package tn.esprit.test.stroke_backend.dto.study;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import tn.esprit.test.stroke_backend.entities.Modality;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StudyRequest {

    @NotBlank
    String studyCode;

    @NotNull
    LocalDate studyDate;

    @NotNull
    Modality modality;
}