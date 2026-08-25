package tn.esprit.test.stroke_backend.integration;

import java.util.List;

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
public class FastApiPredictionResponse {

    String status;

    String filename;

    String prediction_file;

    String preview_file;

    List<Integer> prediction_shape;

    Integer preview_slice;

    LesionResponse lesion;

    Double execution_time_seconds;

    String overlay_file;


    // =========================================================
    // LESION
    // =========================================================

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LesionResponse {

        Boolean detected;

        Integer voxel_count;

        Double volume_mm3;

        CentroidResponse centroid;

        BoundingBoxResponse bounding_box;
    }


    // =========================================================
    // CENTROID
    // =========================================================

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CentroidResponse {

        CoordinateResponse index;

        CoordinateResponse physical;
    }


    // =========================================================
    // COORDINATES
    // =========================================================

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CoordinateResponse {

        Double x;

        Double y;

        Double z;
    }


    // =========================================================
    // BOUNDING BOX
    // =========================================================

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BoundingBoxResponse {

        Integer min_x;

        Integer max_x;

        Integer min_y;

        Integer max_y;

        Integer min_z;

        Integer max_z;
    }
}
