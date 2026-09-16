package tn.esprit.test.stroke_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;
import tn.esprit.test.stroke_backend.services.servicesInterface.IMedicalReportService;

@RestController
@RequestMapping("/api/medical-reports")
@RequiredArgsConstructor
public class MedicalReportController {

    private final IMedicalReportService medicalReportService;

    @PostMapping("/generate/{predictionId}")
    public ResponseEntity<?> generateMedicalReport(@PathVariable Long predictionId) {
    try {
        String reportPath =
                medicalReportService.generateMedicalReport(
                        predictionId
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of(
                        "message", "Medical report generated successfully",
                        "reportPath", reportPath
                ));

    } catch (IllegalArgumentException exception) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "error", "Prediction not found",
                        "message", exception.getMessage()
                ));

    } catch (IllegalStateException exception) {

        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(Map.of(
                        "error", "Failed to generate medical report",
                        "message", exception.getMessage()
                ));

    } catch (Exception exception) {

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "error", "Internal server error",
                        "message", "An unexpected error occurred"
                ));
    }
}


//     public record MedicalReportResponse(
//             String message,
//             String reportPath
//     ) {
//     }

//     public record ErrorResponse(
//             String error,
//             String message
//     ) {
//     }
    
}
