package tn.esprit.test.stroke_backend.services;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tn.esprit.test.stroke_backend.dto.study.PredictionResponseDTO;
import tn.esprit.test.stroke_backend.dto.study.StudyRequest;
import tn.esprit.test.stroke_backend.dto.study.StudyResponseDTO;
import tn.esprit.test.stroke_backend.entities.Modality;
import tn.esprit.test.stroke_backend.entities.Patient;
import tn.esprit.test.stroke_backend.entities.Prediction;
import tn.esprit.test.stroke_backend.entities.Role;
import tn.esprit.test.stroke_backend.entities.Studies;
import tn.esprit.test.stroke_backend.entities.StudiesStatus;
import tn.esprit.test.stroke_backend.entities.User;

import tn.esprit.test.stroke_backend.exceptions.ForbiddenException;
import tn.esprit.test.stroke_backend.exceptions.PatientNotFoundException;
import tn.esprit.test.stroke_backend.exceptions.StudiesCodeAlreadyExistsException;
import tn.esprit.test.stroke_backend.exceptions.StudiesNotFoundException;
import tn.esprit.test.stroke_backend.integration.FastApiPredictionResponse;
import tn.esprit.test.stroke_backend.integration.FastApiService;
import tn.esprit.test.stroke_backend.repositories.PatientRepository;
import tn.esprit.test.stroke_backend.repositories.PredictionRepository;
import tn.esprit.test.stroke_backend.repositories.StudiesRepository;

import tn.esprit.test.stroke_backend.services.servicesInterface.IStudiesService;

// Garde ici le BON package selon ton projet


import tn.esprit.test.stroke_backend.storage.FileStorageService;


@Service
@RequiredArgsConstructor
@Slf4j
public class StudiesService implements IStudiesService {

 private final StudiesRepository studiesRepository;

    private final PredictionRepository predictionRepository;
    private final PatientRepository patientRepository;
    private final CurrentUserService currentUserService;
    private final FileStorageService fileStorageService;
    private final FastApiService fastApiService;


    // CREATE STUDY
    public Studies createStudy(Long patientId, StudyRequest request) {

        User doctor = currentUserService.getCurrentUser();

        if (doctor.getRole() != Role.DOCTOR) {
            throw new ForbiddenException(
                    "Only a doctor can create a study"
            );
        }

        Patient patient = patientRepository
                .findByIdAndDoctor(patientId, doctor)
                .orElseThrow(() ->
                        new PatientNotFoundException(
                                "Patient not found"
                        ));

        if (studiesRepository
                .existsByStudyCode(request.getStudyCode())) {

            throw new StudiesCodeAlreadyExistsException(
                    "This study code already exists"
            );
        }

        Studies study = new Studies();

        study.setStudyCode(
                request.getStudyCode()
        );

        study.setStudyDate(
                request.getStudyDate()
        );

        study.setModality(
                request.getModality()
        );

        study.setPatient(patient);

        return studiesRepository.save(study);
    }

    // GET PATIENT STUDIES
    public List<Studies> getPatientStudies(Long patientId) {

        User doctor = currentUserService.getCurrentUser();

        if (doctor.getRole() != Role.DOCTOR) {
            throw new ForbiddenException(
                    "Only a doctor can access studies"
            );
        }

        // Vérifier que le patient appartient
        // bien au médecin connecté
        patientRepository
                .findByIdAndDoctor(patientId, doctor)
                .orElseThrow(() ->
                        new PatientNotFoundException(
                                "Patient not found"
                        ));

        return studiesRepository
                .findAllByPatientIdAndPatientDoctor(
                        patientId,
                        doctor
                );
    }

    // GET STUDY BY ID
    public Studies getStudy(Long studyId) {

        User doctor = currentUserService.getCurrentUser();

        if (doctor.getRole() != Role.DOCTOR) {
            throw new ForbiddenException(
                    "Only a doctor can access studies"
            );
        }

        return studiesRepository
                .findByIdAndPatientDoctor(
                        studyId,
                        doctor
                )
                .orElseThrow(() ->
                        new StudiesNotFoundException(
                                "Study not found"
                        ));
    }

    // =========================================================
    // ANALYZE STUDY
    // =========================================================

    @Override
    public StudyResponseDTO analyzeStudy( Long patientId, MultipartFile file, Modality modality) {

        log.info(
                "Starting MRI analysis - patientId={}, modality={}",
                patientId,
                modality
        );


        // =====================================================
        // 1. GET AUTHENTICATED DOCTOR
        // =====================================================

        User doctor =
                currentUserService.getCurrentUser();


        // =====================================================
        // 2. VERIFY DOCTOR ROLE
        // =====================================================

        if (doctor == null) {

            throw new ForbiddenException(
                    "Utilisateur non authentifié"
            );
        }


        /*
         * Ici ton projet utilise actuellement Role.DOCTOR.
         */
        if (doctor.getRole() == null ||
            !doctor.getRole().name().equals("DOCTOR")) {

            throw new ForbiddenException(
                    "Only a doctor can perform MRI analysis"
            );
        }


        // =====================================================
        // 3. VERIFY FILE
        // =====================================================

        validateFile(file);


        // =====================================================
        // 4. VERIFY MODALITY
        // =====================================================

        if (modality == null) {

            throw new IllegalArgumentException(
                    "La modalité IRM est obligatoire"
            );
        }


        if (modality != Modality.DWI) {

            throw new IllegalArgumentException(
                    "Le modèle actuel accepte uniquement la modalité DWI"
            );
        }


        // =====================================================
        // 5. VERIFY PATIENT
        // =====================================================

        Patient patient =
                patientRepository
                        .findByIdAndDoctor(
                                patientId,
                                doctor
                        )
                        .orElseThrow(() ->
                                new PatientNotFoundException(
                                        "Patient not found"
                                )
                        );


        // =====================================================
        // 6. CREATE STUDY
        // =====================================================

        Studies study = new Studies();

        study.setStudyCode(
                generateStudyCode()
        );

        study.setStudyDate(
                LocalDate.now()
        );

        study.setModality(
                modality
        );

        study.setStatus(
                StudiesStatus.UPLOADED
        );

        study.setPatient(
                patient
        );


        // =====================================================
        // 7. SAVE STUDY
        // =====================================================

        study =
                studiesRepository.save(study);


        log.info(
                "Study created - id={}, code={}",
                study.getId(),
                study.getStudyCode()
        );


        // =====================================================
        // 8. SAVE DWI FILE
        // =====================================================

        try {

            String storagePath =
                    fileStorageService.storeDwiFile(
                            file,
                            patient.getPatientCode(),
                            study.getStudyCode()
                    );


            study.setDwiFileName(
                    file.getOriginalFilename()
            );

            study.setDwiFileSize(
                    file.getSize()
            );

            study.setDwiStoragePath(
                    storagePath
            );


            // =================================================
            // 9. PROCESSING
            // =================================================

            study.setStatus(
                    StudiesStatus.PROCESSING
            );

            study =
                    studiesRepository.save(study);


        } catch (Exception e) {

            log.error(
                    "Error while storing DWI file",
                    e
            );

            study.setStatus(
                    StudiesStatus.FAILED
            );

            study.setErrorMessage(
                    "Impossible de sauvegarder le fichier DWI"
            );

            studiesRepository.save(study);

            return toResponseDTO(study, null);
        }


        // =====================================================
        // 10. CALL FASTAPI
        // =====================================================

        try {

            log.info(
                    "Calling FastAPI for study {}",
                    study.getId()
            );


            Path physicalPath =
                    fileStorageService.getPhysicalPath(
                            study.getDwiStoragePath()
                    );


            FastApiPredictionResponse response =
                    fastApiService.predict(
                            physicalPath
                    );


            // =================================================
            // 11. VERIFY FASTAPI RESPONSE
            // =================================================

            if (response == null) {

                throw new RuntimeException(
                        "FastAPI returned an empty response"
                );
            }


            if (!"success".equalsIgnoreCase(
                    response.getStatus())) {

                throw new RuntimeException(
                        "FastAPI prediction failed"
                );
            }


            // =================================================
            // 12. CREATE PREDICTION
            // =================================================

            Prediction prediction =
                    createPrediction(
                            study,
                            response
                    );


            prediction =
                    predictionRepository.save(
                            prediction
                    );


            // =================================================
            // 13. COMPLETE STUDY
            // =================================================

            study.setStatus(
                    StudiesStatus.COMPLETED
            );

            study.setErrorMessage(
                    null
            );

            study =
                    studiesRepository.save(
                            study
                    );


            log.info(
                    "MRI analysis completed - studyId={}",
                    study.getId()
            );


            return toResponseDTO(
                    study,
                    prediction
            );


        } catch (Exception e) {

            // =================================================
            // FASTAPI ERROR
            // =================================================

            log.error(
                    "FastAPI analysis failed for study {}",
                    study.getId(),
                    e
            );


            study.setStatus(
                    StudiesStatus.FAILED
            );

            study.setErrorMessage(
                    e.getMessage()
            );


            study =
                    studiesRepository.save(
                            study
                    );


            /*
             * L'échec ML est un état métier.
             * On retourne donc la Study avec FAILED
             * plutôt qu'une erreur 500.
             */

            return toResponseDTO(
                    study,
                    null
            );
        }
    }


    // =========================================================
    // VALIDATE FILE
    // =========================================================

    private void validateFile(
            MultipartFile file) {

        if (file == null ||
            file.isEmpty()) {

            throw new IllegalArgumentException(
                    "Le fichier DWI est obligatoire"
            );
        }


        String filename =
                file.getOriginalFilename();


        if (filename == null ||
            filename.isBlank()) {

            throw new IllegalArgumentException(
                    "Nom de fichier invalide"
            );
        }


        String lowerName =
                filename.toLowerCase();


        boolean validExtension =
                lowerName.endsWith(".nii") ||
                lowerName.endsWith(".nii.gz");


        if (!validExtension) {

            throw new IllegalArgumentException(
                    "Le fichier doit être au format .nii ou .nii.gz"
            );
        }


        // 200 MB
        long maxSize =
                200L * 1024L * 1024L;


        if (file.getSize() > maxSize) {

            throw new IllegalArgumentException(
                    "La taille maximale du fichier est de 200 MB"
            );
        }
    }


    // =========================================================
    // CREATE PREDICTION
    // =========================================================

    private Prediction createPrediction( Studies study,  FastApiPredictionResponse response) {

        Prediction prediction =
                new Prediction();


        prediction.setStudy(
                study
        );


        // =====================================================
        // FILES
        // =====================================================

        prediction.setPredictionFile(
                response.getPrediction_file()
        );

        prediction.setPreviewFile(
                response.getPreview_file()
        );

        prediction.setOverlayFile(
                response.getOverlay_file()
        );


        // =====================================================
        // SHAPE
        // =====================================================

        List<Integer> shape =
                response.getPrediction_shape();


        if (shape != null &&
            shape.size() >= 3) {

            prediction.setPredictionShapeX(
                    shape.get(0)
            );

            prediction.setPredictionShapeY(
                    shape.get(1)
            );

            prediction.setPredictionShapeZ(
                    shape.get(2)
            );
        }


        prediction.setPreviewSlice(
                response.getPreview_slice()
        );


        // =====================================================
        // LESION
        // =====================================================

        FastApiPredictionResponse.LesionResponse lesion =
                response.getLesion();


        if (lesion != null) {

            prediction.setLesionDetected(
                    lesion.getDetected()
            );

            prediction.setLesionVoxels(
                    lesion.getVoxel_count()
            );

            prediction.setLesionVolumeMm3(
                    lesion.getVolume_mm3()
            );


            // ===============================================
            // CENTROID
            // ===============================================

            if (lesion.getCentroid() != null) {

                var centroid =
                        lesion.getCentroid();


                if (centroid.getIndex() != null) {

                    prediction.setCentroidIndexX(
                            centroid.getIndex().getX()
                    );

                    prediction.setCentroidIndexY(
                            centroid.getIndex().getY()
                    );

                    prediction.setCentroidIndexZ(
                            centroid.getIndex().getZ()
                    );
                }


                if (centroid.getPhysical() != null) {

                    prediction.setCentroidPhysicalX(
                            centroid.getPhysical().getX()
                    );

                    prediction.setCentroidPhysicalY(
                            centroid.getPhysical().getY()
                    );

                    prediction.setCentroidPhysicalZ(
                            centroid.getPhysical().getZ()
                    );
                }
            }


            // ===============================================
            // BOUNDING BOX
            // ===============================================

            if (lesion.getBounding_box() != null) {

                var box =
                        lesion.getBounding_box();


                prediction.setBoundingBoxMinX(
                        box.getMin_x()
                );

                prediction.setBoundingBoxMaxX(
                        box.getMax_x()
                );

                prediction.setBoundingBoxMinY(
                        box.getMin_y()
                );

                prediction.setBoundingBoxMaxY(
                        box.getMax_y()
                );

                prediction.setBoundingBoxMinZ(
                        box.getMin_z()
                );

                prediction.setBoundingBoxMaxZ(
                        box.getMax_z()
                );
            }
        }


        // =====================================================
        // PROCESSING TIME
        // =====================================================

        prediction.setProcessingTime(
                response.getExecution_time_seconds()
        );


        return prediction;
    }


    // =========================================================
    // GENERATE STUDY CODE
    // =========================================================

    private String generateStudyCode() {

        long count =
                studiesRepository.count() + 1;


        return String.format(
                "S%03d",
                count
        );
    }


    // =========================================================
    // DTO MAPPING
    // =========================================================

    private StudyResponseDTO toResponseDTO(
            Studies study,
            Prediction prediction) {

        StudyResponseDTO dto =
                new StudyResponseDTO();


        dto.setId(
                study.getId()
        );

        dto.setStudyCode(
                study.getStudyCode()
        );

        dto.setStudyDate(
                study.getStudyDate()
        );

        dto.setModality(
                study.getModality()
        );

        dto.setStatus(
                study.getStatus()
        );


        // =====================================================
        // PATIENT
        // =====================================================

        if (study.getPatient() != null) {

            dto.setPatientId(
                    study.getPatient().getId()
            );

            dto.setPatientCode(
                    study.getPatient().getPatientCode()
            );

            dto.setPatientFullName(
                    study.getPatient().getFirstName()
                    + " "
                    + study.getPatient().getLastName()
            );
        }


        // =====================================================
        // FILE
        // =====================================================

        dto.setDwiFileName(
                study.getDwiFileName()
        );

        dto.setDwiFileSize(
                study.getDwiFileSize()
        );


        // =====================================================
        // ERROR
        // =====================================================

        dto.setErrorMessage(
                study.getErrorMessage()
        );


        // =====================================================
        // PREDICTION
        // =====================================================

        if (prediction != null) {

            dto.setPrediction(
                    toPredictionDTO(prediction)
            );
        }


        dto.setCreatedAt(
                study.getCreatedAt()
        );

        dto.setUpdatedAt(
                study.getUpdatedAt()
        );


        return dto;
    }


    // =========================================================
    // PREDICTION DTO
    // =========================================================

    private PredictionResponseDTO toPredictionDTO(
            Prediction prediction) {

        PredictionResponseDTO dto =
                new PredictionResponseDTO();


        dto.setId(
                prediction.getId()
        );


        dto.setPredictionFile(
                prediction.getPredictionFile()
        );

        dto.setPreviewFile(
                prediction.getPreviewFile()
        );

        dto.setOverlayFile(
                prediction.getOverlayFile()
        );


        dto.setPredictionShapeX(
                prediction.getPredictionShapeX()
        );

        dto.setPredictionShapeY(
                prediction.getPredictionShapeY()
        );

        dto.setPredictionShapeZ(
                prediction.getPredictionShapeZ()
        );

        dto.setPreviewSlice(
                prediction.getPreviewSlice()
        );


        dto.setLesionDetected(
                prediction.getLesionDetected()
        );

        dto.setLesionVoxels(
                prediction.getLesionVoxels()
        );

        dto.setLesionVolumeMm3(
                prediction.getLesionVolumeMm3()
        );


        dto.setCentroidIndexX(
                prediction.getCentroidIndexX()
        );

        dto.setCentroidIndexY(
                prediction.getCentroidIndexY()
        );

        dto.setCentroidIndexZ(
                prediction.getCentroidIndexZ()
        );


        dto.setCentroidPhysicalX(
                prediction.getCentroidPhysicalX()
        );

        dto.setCentroidPhysicalY(
                prediction.getCentroidPhysicalY()
        );

        dto.setCentroidPhysicalZ(
                prediction.getCentroidPhysicalZ()
        );


        dto.setBoundingBoxMinX(
                prediction.getBoundingBoxMinX()
        );

        dto.setBoundingBoxMaxX(
                prediction.getBoundingBoxMaxX()
        );

        dto.setBoundingBoxMinY(
                prediction.getBoundingBoxMinY()
        );

        dto.setBoundingBoxMaxY(
                prediction.getBoundingBoxMaxY()
        );

        dto.setBoundingBoxMinZ(
                prediction.getBoundingBoxMinZ()
        );

        dto.setBoundingBoxMaxZ(
                prediction.getBoundingBoxMaxZ()
        );


        dto.setProcessingTime(
                prediction.getProcessingTime()
        );

        dto.setCreatedAt(
                prediction.getCreatedAt()
        );


        return dto;
    }




}