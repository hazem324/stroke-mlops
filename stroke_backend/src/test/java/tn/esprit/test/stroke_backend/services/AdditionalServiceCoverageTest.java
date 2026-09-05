package tn.esprit.test.stroke_backend.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.http.ResponseEntity;

import tn.esprit.test.stroke_backend.entities.Patient;
import tn.esprit.test.stroke_backend.entities.Role;
import tn.esprit.test.stroke_backend.entities.Studies;
import tn.esprit.test.stroke_backend.entities.StudiesStatus;
import tn.esprit.test.stroke_backend.entities.User;
import tn.esprit.test.stroke_backend.repositories.PatientRepository;
import tn.esprit.test.stroke_backend.repositories.UserRepository;
import tn.esprit.test.stroke_backend.storage.FileStorageService;

@ExtendWith(MockitoExtension.class)
class AdditionalServiceCoverageTest {

    @TempDir
    Path tempDir;

    @Mock
    PatientRepository patientRepository;

    @Mock
    CurrentUserService currentUserService;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    DashboardService dashboardService;

    @Test
    void fileStorage_shouldStoreAnalysisAndDwiFiles() throws Exception {
        FileStorageService storage = new FileStorageService(tempDir.toString());
        MockMultipartFile file = new MockMultipartFile("file", "scan.nii.gz", "application/gzip", new byte[] {1, 2});
        String dwiPath = storage.storeDwiFile(file, "10", "S001");
        assertTrue(storage.exists(dwiPath));
        assertEquals(Path.of("patients", "10", "studies", "S001", "dwi.nii.gz").toString().replace("\\", "/"), dwiPath);
        FileStorageService.AnalysisPaths paths = storage.storeAnalysisFiles(new byte[] {1}, new byte[] {2}, new byte[] {3}, "10", "S001");
        assertTrue(storage.exists(paths.predictionFile()));
        assertTrue(Files.exists(storage.getPhysicalPath(paths.previewFile())));
    }

    @Test
    void dashboard_shouldReturnStatisticsAndRecentAnalyses() {
        User doctor = new User();
        doctor.setId(5L);
        when(currentUserService.getCurrentUser()).thenReturn(doctor);
        when(patientRepository.countPatientsByDoctor(5L)).thenReturn(2L);
        when(patientRepository.countStudiesByDoctor(5L)).thenReturn(3L);
        when(patientRepository.countCompletedAnalysesByDoctor(5L)).thenReturn(1L);
        when(patientRepository.countPendingAnalysesByDoctor(5L)).thenReturn(2L);
        ResponseEntity<?> stats = dashboardService.getStatistics();
        assertEquals(200, stats.getStatusCode().value());
        when(patientRepository.countPatientsByDoctor(5L)).thenThrow(new RuntimeException("db"));
        assertEquals(500, dashboardService.getStatistics().getStatusCode().value());

        Patient patient = new Patient();
        patient.setId(7L);
        patient.setPatientCode("P7");
        patient.setFirstName("First");
        patient.setLastName("Last");
        Studies study = new Studies();
        study.setId(9L);
        study.setPatient(patient);
        study.setStudyCode("S9");
        study.setStatus(StudiesStatus.COMPLETED);
        when(patientRepository.findRecentStudiesByDoctor(any(), any())).thenReturn(List.of(study));
        assertEquals(200, dashboardService.getRecentAnalyses().getStatusCode().value());
        when(patientRepository.findRecentStudiesByDoctor(any(), any())).thenThrow(new RuntimeException("db"));
        assertEquals(500, dashboardService.getRecentAnalyses().getStatusCode().value());
    }

    @Test
    void jwtService_shouldGenerateSignedToken() {
        User user = new User();
        user.setId(12L);
        user.setEmail("doctor@example.com");
        user.setRole(Role.DOCTOR);
        JwtService service = new JwtService("a-very-long-secret-key-for-tests-123456789", 60000L);
        String token = service.generateToken(user);
        assertNotNull(token);
        assertEquals(2, token.chars().filter(ch -> ch == '.').count());
    }

    @Test
    void userEntity_shouldExposeLifecycleFields() throws Exception {
        User user = new User();
        user.setFirstName("A");
        user.setLastName("B");
        user.setEnabled(true);
        user.setAcceptedTerms(true);
        assertEquals("A", user.getFirstName());
        assertTrue(user.isEnabled());
        var create = User.class.getDeclaredMethod("onCreate");
        create.setAccessible(true);
        create.invoke(user);
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
        LocalDateTime created = user.getCreatedAt();
        var update = User.class.getDeclaredMethod("onUpdate");
        update.setAccessible(true);
        update.invoke(user);
        assertEquals(created, user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
    }
}
