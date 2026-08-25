package tn.esprit.test.stroke_backend.entities;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String patientCode;
    String firstName;
    String lastName;
    LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    Sex sex;

    Integer age;
    Double weight;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    User doctor;

    String phoneNumber;

    @JsonIgnore
    @OneToMany(mappedBy = "patient")
    List<Studies> studies;
}