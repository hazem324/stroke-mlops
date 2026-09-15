package tn.esprit.test.stroke_backend.services;

public record MedicalReportContent(
        String reportRef,

        String centerName,
        String centerAddress,
        String centerPhone,

        String doctorName,
        String doctorTitle,

        String patientCode,
        String patientName,
        String dateOfBirth,
        String age,
        String sex,

        String studyCode,
        String studyDate,
        String modality,

        String indication,
        String technique,
        String resultats,
        String conclusion
) {}
