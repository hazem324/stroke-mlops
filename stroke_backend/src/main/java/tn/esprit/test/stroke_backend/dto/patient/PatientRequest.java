package tn.esprit.test.stroke_backend.dto.patient;

import java.time.LocalDate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import tn.esprit.test.stroke_backend.entities.Sex;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PatientRequest {
    
    String patientCode;
    String firstName;
    String lastName;
    LocalDate dateOfBirth;
    Sex sex;
    Integer age;
    Double weight;
    String phoneNumber;
}
