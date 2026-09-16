package tn.esprit.test.stroke_backend.services.servicesInterface;

import java.io.IOException;

public interface IFileStorageService {

    String storeMedicalReport(
            byte[] pdfBytes,
            String patientCode,
            String studyCode
    ) throws IOException;

    byte[] readMedicalReport(
            String reportPath
    ) throws IOException;
}