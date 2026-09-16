package tn.esprit.test.stroke_backend.services;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tn.esprit.test.stroke_backend.entities.GeneratedSections;
import tn.esprit.test.stroke_backend.entities.MedicalReport;
import tn.esprit.test.stroke_backend.entities.MedicalReportContent;
import tn.esprit.test.stroke_backend.entities.Patient;
import tn.esprit.test.stroke_backend.entities.Prediction;
import tn.esprit.test.stroke_backend.entities.Studies;
import tn.esprit.test.stroke_backend.entities.User;
import tn.esprit.test.stroke_backend.repositories.MedicalReportRepository;
import tn.esprit.test.stroke_backend.repositories.PredictionRepository;
import tn.esprit.test.stroke_backend.services.servicesInterface.IGeminiReportService;
import tn.esprit.test.stroke_backend.services.servicesInterface.IMedicalReportService;
import tn.esprit.test.stroke_backend.storage.FileStorageService;

@Service
@Transactional
public class MedicalReportService implements IMedicalReportService {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PredictionRepository predictionRepository;
    private final MedicalReportRepository medicalReportRepository;
    private final IGeminiReportService geminiReportService;
    private final FileStorageService fileStorageService;
    private final MedicalReportPdfRenderer pdfRenderer;

    public MedicalReportService(
            PredictionRepository predictionRepository,
            MedicalReportRepository medicalReportRepository,
            IGeminiReportService geminiReportService,
            FileStorageService fileStorageService,
            MedicalReportPdfRenderer pdfRenderer
    ) {
        this.predictionRepository = predictionRepository;
        this.medicalReportRepository = medicalReportRepository;
        this.geminiReportService = geminiReportService;
        this.fileStorageService = fileStorageService;
        this.pdfRenderer = pdfRenderer;
    }

    @Override
    public String generateMedicalReport(Long predictionId) {

        if (predictionId == null) {
            throw new IllegalArgumentException(
                    "Prediction id cannot be null"
            );
        }

        Prediction prediction = predictionRepository
                .findById(predictionId)
                .orElseThrow(() -> new RuntimeException(
                        "Prediction not found with id: " + predictionId
                ));

        Studies study = prediction.getStudy();

        if (study == null) {
            throw new IllegalStateException(
                    "No study associated with this prediction"
            );
        }

        Patient patient = study.getPatient();

        if (patient == null) {
            throw new IllegalStateException(
                    "No patient associated with this study"
            );
        }

        String patientCode = patient.getPatientCode();
        String studyCode = study.getStudyCode();

        if (patientCode == null || patientCode.isBlank()) {
            throw new IllegalStateException(
                    "Patient code is missing"
            );
        }

        if (studyCode == null || studyCode.isBlank()) {
            throw new IllegalStateException(
                    "Study code is missing"
            );
        }

        GeneratedSections sections =
                geminiReportService.generateReport(prediction);

        validateGeneratedSections(sections);

        MedicalReportContent content =
                buildContent(patient, study, sections);

        byte[] pdfBytes = pdfRenderer.render(content);

        try {

            String reportPath =
                    fileStorageService.storeMedicalReport(
                            pdfBytes,
                            patientCode,
                            studyCode
                    );

            MedicalReport medicalReport =
                    medicalReportRepository
                            .findByStudy(study)
                            .orElse(new MedicalReport());

            medicalReport.setStudy(study);
            medicalReport.setReportPath(reportPath);

            medicalReportRepository.save(medicalReport);

            return reportPath;

        } catch (IOException exception) {

            throw new RuntimeException(
                    "Failed to save medical report PDF: "
                            + exception.getMessage(),
                    exception
            );
        }
    }

    private void validateGeneratedSections(
            GeneratedSections sections
    ) {

        if (sections == null) {
            throw new IllegalStateException(
                    "Gemini generated sections are null"
            );
        }

        if (sections.resultats() == null
                || sections.resultats().isBlank()) {

            throw new IllegalStateException(
                    "Gemini generated an empty Résultats section"
            );
        }

        if (sections.conclusion() == null
                || sections.conclusion().isBlank()) {

            throw new IllegalStateException(
                    "Gemini generated an empty Conclusion section"
            );
        }
    }

    private MedicalReportContent buildContent(
            Patient patient,
            Studies study,
            GeneratedSections sections
    ) {

        String indication =
                "Bilan d’une suspicion d’accident vasculaire "
                        + "cérébral ischémique.";

        String technique =
                "IRM cérébrale avec étude de la séquence de diffusion DWI. "
                        + "L’examen porte sur la recherche d’anomalies focales "
                        + "de diffusion pouvant être évocatrices d’une lésion "
                        + "ischémique récente.";

        User doctor = patient.getDoctor();

        return new MedicalReportContent(
                "RPT-" + study.getStudyCode(),

                "CENTRE D’IMAGERIE MÉDICALE",
                "Service de radiologie — Unité de neuro-imagerie",

                 doctor !=null ? "Dr " + doctor.getFirstName() + " " + doctor.getLastName() : "Médecin non renseigné",
                "Médecin radiologue",

                safe(patient.getPatientCode()),
                safe(patient.getFirstName())
                        + " "
                        + safe(patient.getLastName()),

                patient.getDateOfBirth() != null
                        ? patient.getDateOfBirth().format(DATE_FMT)
                        : "Non renseignée",

                patient.getAge() != null
                        ? patient.getAge().toString()
                        : "Non renseigné",

                formatSex(
                        patient.getSex() != null
                                ? patient.getSex().toString()
                                : null
                ),

                safe(study.getStudyCode()),

                study.getStudyDate() != null
                        ? study.getStudyDate().format(DATE_FMT)
                        : "Non renseignée",

                "IRM cérébrale — séquence de diffusion DWI",

                indication,
                technique,
                sections.resultats(),
                sections.conclusion()
        );
    }

    private String formatSex(String sex) {

        if (sex == null || sex.isBlank()) {
            return "Non renseigné";
        }

        return switch (sex.toUpperCase()) {
            case "MALE", "M" -> "Masculin";
            case "FEMALE", "F" -> "Féminin";
            default -> sex;
        };
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}