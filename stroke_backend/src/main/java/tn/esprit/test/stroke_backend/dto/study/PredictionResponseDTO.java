package tn.esprit.test.stroke_backend.dto.study;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PredictionResponseDTO {

    Long id;

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
}