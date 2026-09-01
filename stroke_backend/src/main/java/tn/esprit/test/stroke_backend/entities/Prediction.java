package tn.esprit.test.stroke_backend.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.JoinColumn;

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
public class Prediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne
    @JoinColumn(
        name = "study_id",
        nullable = false,
        unique = true
    )
    Studies study;

    String predictionFile;

    String previewFile;

    String overlayFile;

    Integer predictionShapeX;

    Integer predictionShapeY;

    Integer predictionShapeZ;

    Integer previewSlice;

    Boolean lesionDetected;

    Integer lesionVoxels;

    Double lesionVolumeMm3;

    Double centroidIndexX;

    Double centroidIndexY;

    Double centroidIndexZ;

    Double centroidPhysicalX;

    Double centroidPhysicalY;

    Double centroidPhysicalZ;

    Integer boundingBoxMinX;

    Integer boundingBoxMaxX;

    Integer boundingBoxMinY;

    Integer boundingBoxMaxY;

    Integer boundingBoxMinZ;

    Integer boundingBoxMaxZ;

    Double processingTime;

    LocalDateTime createdAt;


    @PrePersist
    void onCreate() {

        createdAt = LocalDateTime.now();
    }
}