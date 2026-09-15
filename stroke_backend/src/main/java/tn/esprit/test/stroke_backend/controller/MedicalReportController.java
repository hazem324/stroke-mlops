package tn.esprit.test.stroke_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;
import tn.esprit.test.stroke_backend.services.servicesInterface.IMedicalReportService;

@RestController
@RequestMapping("/api/medical-reports")
@RequiredArgsConstructor
public class MedicalReportController {

    private final IMedicalReportService medicalReportService;

    @PostMapping("/generate/{predictionId}")
    public ResponseEntity<?> generateMedicalReport(
            @PathVariable Long predictionId) {

        try {

            String reportPath =
                    medicalReportService.generateMedicalReport(predictionId);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new MedicalReportResponse(
                            "Medical report generated successfully",
                            reportPath
                    ));

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Failed to generate medical report",
                            e.getMessage()
                    ));
        }
    }

    public record MedicalReportResponse(
            String message,
            String reportPath
    ) {
    }

    public record ErrorResponse(
            String error,
            String message
    ) {
    }
    
}
