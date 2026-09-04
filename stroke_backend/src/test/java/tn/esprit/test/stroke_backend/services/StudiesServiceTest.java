package tn.esprit.test.stroke_backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import tn.esprit.test.stroke_backend.exceptions.PatientNotFoundException;
import tn.esprit.test.stroke_backend.repositories.PatientRepository;
import tn.esprit.test.stroke_backend.repositories.PredictionRepository;
import tn.esprit.test.stroke_backend.repositories.StudiesRepository;
import tn.esprit.test.stroke_backend.storage.FileStorageService;

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
}
