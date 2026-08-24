package tn.esprit.test.stroke_backend.dto.patient;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import tn.esprit.test.stroke_backend.entities.Sex;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PatientUpdateRequest {

    @NotBlank
    String firstName;

    @NotBlank
    String lastName;

    @NotNull
    LocalDate dateOfBirth;

    @NotNull
    Sex sex;

    @Positive
    Integer age;

    @Positive
    Double weight;
    
    String phoneNumber;

}