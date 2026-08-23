package tn.esprit.test.stroke_backend.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Studies {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String studyCode;
    LocalDate studyDate;

    @Enumerated(EnumType.STRING)
    Modality modality;

    @Enumerated(EnumType.STRING)
    StudiesStatus status;

    @ManyToOne
    Patient patient;

    LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();

        if (status == null) {
            status = StudiesStatus.READY;
        }
    }
}