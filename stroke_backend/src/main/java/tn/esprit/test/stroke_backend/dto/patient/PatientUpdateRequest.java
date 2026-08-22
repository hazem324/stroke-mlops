package tn.esprit.test.stroke_backend.dto.patient;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.Getter;
import lombok.Setter;

import tn.esprit.test.stroke_backend.entities.Sex;

@Getter
@Setter
public class PatientUpdateRequest {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotNull
    private LocalDate dateOfBirth;

    @NotNull
    private Sex sex;

    @Positive
    private Integer age;

    @Positive
    private Double weight;
}