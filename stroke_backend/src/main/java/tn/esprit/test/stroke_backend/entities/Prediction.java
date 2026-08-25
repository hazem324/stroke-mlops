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


    // =========================================================
    // STUDY
    // =========================================================

    @OneToOne
    @JoinColumn(
        name = "study_id",
        nullable = false,
        unique = true
    )
    Studies study;


    // =========================================================
    // FILES
    // =========================================================

    String predictionFile;

    String previewFile;

    String overlayFile;


    // =========================================================
    // PREDICTION SHAPE
    // =========================================================

    Integer predictionShapeX;

    Integer predictionShapeY;

    Integer predictionShapeZ;

    Integer previewSlice;


    // =========================================================
    // LESION
    // =========================================================

    Boolean lesionDetected;

    Integer lesionVoxels;

    Double lesionVolumeMm3;


    // =========================================================
    // CENTROID - INDEX
    // =========================================================

    Double centroidIndexX;

    Double centroidIndexY;

    Double centroidIndexZ;


    // =========================================================
    // CENTROID - PHYSICAL
    // =========================================================

    Double centroidPhysicalX;

    Double centroidPhysicalY;

    Double centroidPhysicalZ;


    // =========================================================
    // BOUNDING BOX
    // =========================================================

    Integer boundingBoxMinX;

    Integer boundingBoxMaxX;

    Integer boundingBoxMinY;

    Integer boundingBoxMaxY;

    Integer boundingBoxMinZ;

    Integer boundingBoxMaxZ;


    // =========================================================
    // EXECUTION
    // =========================================================

    Double processingTime;


    // =========================================================
    // DATE
    // =========================================================

    LocalDateTime createdAt;


    @PrePersist
    void onCreate() {

        createdAt = LocalDateTime.now();
    }
}