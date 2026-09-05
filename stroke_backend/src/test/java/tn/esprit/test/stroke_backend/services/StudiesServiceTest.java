package tn.esprit.test.stroke_backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import tn.esprit.test.stroke_backend.dto.study.StudyRequest;
import tn.esprit.test.stroke_backend.entities.Modality;
import tn.esprit.test.stroke_backend.entities.Patient;
import tn.esprit.test.stroke_backend.entities.Role;
import tn.esprit.test.stroke_backend.entities.Studies;
import tn.esprit.test.stroke_backend.entities.User;
import tn.esprit.test.stroke_backend.entities.StudiesStatus;
import tn.esprit.test.stroke_backend.exceptions.PatientNotFoundException;
import tn.esprit.test.stroke_backend.exceptions.ForbiddenException;
import tn.esprit.test.stroke_backend.exceptions.StudiesCodeAlreadyExistsException;
import tn.esprit.test.stroke_backend.exceptions.StudiesNotFoundException;
import tn.esprit.test.stroke_backend.repositories.PatientRepository;
import tn.esprit.test.stroke_backend.repositories.PredictionRepository;
import tn.esprit.test.stroke_backend.repositories.StudiesRepository;
import tn.esprit.test.stroke_backend.storage.FileStorageService;
import tn.esprit.test.stroke_backend.integration.FastApiPredictionResponse;
import tn.esprit.test.stroke_backend.integration.FastApiService.FastApiPredictionResult;

@ExtendWith(MockitoExtension.class)
class StudiesServiceTest {

    @Mock
    private StudiesRepository studiesRepository;

    @Mock
    private PredictionRepository predictionRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private tn.esprit.test.stroke_backend.integration.FastApiService fastApiService;

    @InjectMocks
    private StudiesService studiesService;

    @Test
    void createStudy_shouldPersistStudy_whenDoctorOwnsPatient() {
        User doctor = new User();
        doctor.setId(1L);
        doctor.setRole(Role.DOCTOR);

        Patient patient = new Patient();
        patient.setId(10L);
        patient.setDoctor(doctor);

        StudyRequest request = new StudyRequest();
        request.setStudyCode("S001");
        request.setStudyDate(LocalDate.now());
        request.setModality(Modality.DWI);

        when(currentUserService.getCurrentUser()).thenReturn(doctor);
        when(patientRepository.findByIdAndDoctor(10L, doctor)).thenReturn(Optional.of(patient));
        when(studiesRepository.existsByStudyCode("S001")).thenReturn(false);
        when(studiesRepository.save(org.mockito.ArgumentMatchers.any(Studies.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Studies study = studiesService.createStudy(10L, request);

        assertEquals("S001", study.getStudyCode());
        assertEquals(Modality.DWI, study.getModality());
    }

    @Test
    void createStudy_shouldReject_whenPatientIsNotOwnedByDoctor() {
        User doctor = new User();
        doctor.setRole(Role.DOCTOR);

        StudyRequest request = new StudyRequest();
        request.setStudyCode("S002");
        request.setStudyDate(LocalDate.now());
        request.setModality(Modality.DWI);

        when(currentUserService.getCurrentUser()).thenReturn(doctor);
        when(patientRepository.findByIdAndDoctor(99L, doctor)).thenReturn(Optional.empty());

        assertThrows(PatientNotFoundException.class, () -> studiesService.createStudy(99L, request));
    }

    @Test
    void studyQueries_shouldRejectNonDoctorsAndMissingStudies() {
        User admin = new User();
        admin.setRole(Role.ADMIN);
        when(currentUserService.getCurrentUser()).thenReturn(admin);
        assertThrows(ForbiddenException.class, () -> studiesService.getPatientStudies(1L));
        assertThrows(ForbiddenException.class, () -> studiesService.getStudy(1L));

        User doctor = new User();
        doctor.setRole(Role.DOCTOR);
        when(currentUserService.getCurrentUser()).thenReturn(doctor);
        when(studiesRepository.findByIdAndPatientDoctor(1L, doctor)).thenReturn(Optional.empty());
        assertThrows(StudiesNotFoundException.class, () -> studiesService.getStudy(1L));
    }

    @Test
    void createStudy_shouldRejectDuplicateCode() {
        User doctor = new User();
        doctor.setRole(Role.DOCTOR);
        Patient patient = new Patient();
        patient.setDoctor(doctor);
        StudyRequest request = new StudyRequest();
        request.setStudyCode("DUP");
        when(currentUserService.getCurrentUser()).thenReturn(doctor);
        when(patientRepository.findByIdAndDoctor(1L, doctor)).thenReturn(Optional.of(patient));
        when(studiesRepository.existsByStudyCode("DUP")).thenReturn(true);
        assertThrows(StudiesCodeAlreadyExistsException.class, () -> studiesService.createStudy(1L, request));
    }

    @Test
    void getPatientStudies_shouldReturnStudies_forDoctor() {
        User doctor = new User();
        doctor.setRole(Role.DOCTOR);

        Patient patient = new Patient();
        patient.setId(12L);
        patient.setDoctor(doctor);

        Studies study = new Studies();
        study.setId(34L);
        study.setStudyCode("S003");

        when(currentUserService.getCurrentUser()).thenReturn(doctor);
        when(patientRepository.findByIdAndDoctor(12L, doctor)).thenReturn(Optional.of(patient));
        when(studiesRepository.findAllByPatientIdAndPatientDoctor(12L, doctor)).thenReturn(List.of(study));

        List<Studies> result = studiesService.getPatientStudies(12L);

        assertEquals(1, result.size());
        assertEquals("S003", result.get(0).getStudyCode());
    }

    @Test
    void analyzeStudy_shouldRejectEmptyFile() {
        User doctor = new User();
        doctor.setRole(Role.DOCTOR);

        when(currentUserService.getCurrentUser()).thenReturn(doctor);

        MultipartFile file = new org.springframework.mock.web.MockMultipartFile(
            "file",
            "empty.nii.gz",
            "application/octet-stream",
            new byte[0]
        );

        assertThrows(IllegalArgumentException.class, () -> studiesService.analyzeStudy(1L, file, Modality.DWI));
    }

    @Test
    void analyzeStudy_shouldValidateFilenameModalityAndPatient() {
        User doctor = new User();
        doctor.setRole(Role.DOCTOR);
        when(currentUserService.getCurrentUser()).thenReturn(doctor);
        MultipartFile badName = new org.springframework.mock.web.MockMultipartFile("file", "scan.txt", "application/octet-stream", new byte[] {1});
        assertThrows(IllegalArgumentException.class, () -> studiesService.analyzeStudy(1L, badName, Modality.DWI));
        MultipartFile good = new org.springframework.mock.web.MockMultipartFile("file", "scan.nii.gz", "application/octet-stream", new byte[] {1});
        assertThrows(IllegalArgumentException.class, () -> studiesService.analyzeStudy(1L, good, null));
        when(patientRepository.findByIdAndDoctor(1L, doctor)).thenReturn(Optional.empty());
        assertThrows(PatientNotFoundException.class, () -> studiesService.analyzeStudy(1L, good, Modality.DWI));
    }

    @Test
    void getStudyById_shouldReturnNotFoundAndMappedStudy() {
        when(studiesRepository.findById(9L)).thenReturn(Optional.empty());
        assertEquals(404, studiesService.getStudyById(9L).getStatusCode().value());

        Patient patient = new Patient();
        patient.setId(3L);
        patient.setPatientCode("P3");
        patient.setFirstName("A");
        patient.setLastName("B");
        Studies study = new Studies();
        study.setId(9L);
        study.setStudyCode("S9");
        study.setPatient(patient);
        study.setModality(Modality.DWI);
        when(studiesRepository.findById(9L)).thenReturn(Optional.of(study));
        assertEquals("S9", studiesService.getStudyById(9L).getBody().getStudyCode());
    }

    @Test
    void analyzeStudy_shouldCompleteAndMapPrediction() throws Exception {
        User doctor = new User();
        doctor.setId(2L);
        doctor.setRole(Role.DOCTOR);
        Patient patient = new Patient();
        patient.setId(3L);
        patient.setPatientCode("P3");
        patient.setFirstName("A");
        patient.setLastName("B");
        MultipartFile file = new org.springframework.mock.web.MockMultipartFile("file", "scan.nii.gz", "application/octet-stream", new byte[] {1, 2});
        Studies saved = new Studies();
        saved.setId(11L);
        when(currentUserService.getCurrentUser()).thenReturn(doctor);
        when(patientRepository.findByIdAndDoctor(3L, doctor)).thenReturn(Optional.of(patient));
        when(studiesRepository.count()).thenReturn(0L);
        when(studiesRepository.save(org.mockito.ArgumentMatchers.any(Studies.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(fileStorageService.storeDwiFile(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString())).thenReturn("patients/3/studies/S001/dwi.nii.gz");
        when(fileStorageService.getPhysicalPath(org.mockito.ArgumentMatchers.anyString())).thenReturn(java.nio.file.Path.of("scan.nii.gz"));
        FileStorageService.AnalysisPaths paths = new FileStorageService.AnalysisPaths("prediction.nii.gz", "overlay.nii.gz", "preview.png");
        FastApiPredictionResponse.LesionResponse lesion = new FastApiPredictionResponse.LesionResponse(true, 4, 12.5,
                new FastApiPredictionResponse.CentroidResponse(
                        new FastApiPredictionResponse.CoordinateResponse(1.0, 2.0, 3.0),
                        new FastApiPredictionResponse.CoordinateResponse(4.0, 5.0, 6.0)),
                new FastApiPredictionResponse.BoundingBoxResponse(0, 2, 1, 3, 2, 4));
        FastApiPredictionResponse response = new FastApiPredictionResponse("success", "scan.nii.gz", "prediction.nii.gz", "preview.png", List.of(4, 5, 6), 2, lesion, 1.5, "overlay.nii.gz");
        when(fastApiService.predictAndFetchFiles(org.mockito.ArgumentMatchers.any())).thenReturn(new FastApiPredictionResult(response, new byte[] {1}, new byte[] {2}, new byte[] {3}));
        when(fileStorageService.storeAnalysisFiles(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString())).thenReturn(paths);
        when(predictionRepository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = studiesService.analyzeStudy(3L, file, Modality.DWI);
        assertEquals(StudiesStatus.COMPLETED, result.getStatus());
        assertNotNull(result.getPrediction());
        assertEquals(4, result.getPrediction().getPredictionShapeX());
    }

    @Test
    void analyzeStudy_shouldReturnFailedWhenStorageFails() throws Exception {
        User doctor = new User();
        doctor.setId(2L);
        doctor.setRole(Role.DOCTOR);
        Patient patient = new Patient();
        patient.setId(3L);
        when(currentUserService.getCurrentUser()).thenReturn(doctor);
        when(patientRepository.findByIdAndDoctor(3L, doctor)).thenReturn(Optional.of(patient));
        when(studiesRepository.count()).thenReturn(0L);
        when(studiesRepository.save(org.mockito.ArgumentMatchers.any(Studies.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(fileStorageService.storeDwiFile(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString())).thenThrow(new java.io.IOException("disk"));
        MultipartFile file = new org.springframework.mock.web.MockMultipartFile("file", "scan.nii.gz", "application/octet-stream", new byte[] {1});
        var result = studiesService.analyzeStudy(3L, file, Modality.DWI);
        assertEquals(StudiesStatus.FAILED, result.getStatus());
    }
}
