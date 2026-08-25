package tn.esprit.test.stroke_backend.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
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

    String dwiFileName;

    String dwiStoragePath;

    Long dwiFileSize;

    String errorMessage;

    @OneToOne(mappedBy = "study")
    @JsonIgnore
    Prediction prediction;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

        if (status == null) {
            status = StudiesStatus.UPLOADED;
        }
    }

    @PreUpdate
    void onUpdate() {

        updatedAt = LocalDateTime.now();
    }
}