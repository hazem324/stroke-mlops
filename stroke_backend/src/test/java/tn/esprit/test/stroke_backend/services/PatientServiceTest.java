package tn.esprit.test.stroke_backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tn.esprit.test.stroke_backend.dto.patient.PatientRequest;
import tn.esprit.test.stroke_backend.dto.patient.PatientUpdateRequest;
import tn.esprit.test.stroke_backend.entities.Patient;
import tn.esprit.test.stroke_backend.entities.Role;
import tn.esprit.test.stroke_backend.entities.Sex;
import tn.esprit.test.stroke_backend.entities.User;
import tn.esprit.test.stroke_backend.exceptions.ForbiddenException;
import tn.esprit.test.stroke_backend.exceptions.PatientCodeAlreadyExistsException;
import tn.esprit.test.stroke_backend.exceptions.PatientNotFoundException;
import tn.esprit.test.stroke_backend.repositories.PatientRepository;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private PatientService patientService;

    @Test
    void createPatient_shouldSavePatient_whenDoctorIsAuthenticated() {
        User doctor = new User();
        doctor.setId(1L);
        doctor.setRole(Role.DOCTOR);

        PatientRequest request = new PatientRequest();
        request.setPatientCode("P-1001");
        request.setFirstName("Jean");
        request.setLastName("Dupont");
        request.setDateOfBirth(LocalDate.of(1980, 1, 10));
        request.setSex(Sex.M);
        request.setAge(45);
        request.setWeight(75.5);
        request.setPhoneNumber("0612345678");

        when(currentUserService.getCurrentUser()).thenReturn(doctor);
        when(patientRepository.existsByPatientCode("P-1001")).thenReturn(false);

        Patient savedPatient = new Patient();
        savedPatient.setId(99L);
        savedPatient.setPatientCode("P-1001");
        savedPatient.setDoctor(doctor);

        when(patientRepository.save(any(Patient.class))).thenReturn(savedPatient);

        Patient result = patientService.createPatient(request);

        assertEquals("P-1001", result.getPatientCode());
        assertEquals(doctor, result.getDoctor());
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void createPatient_shouldReject_whenUserIsNotDoctor() {
        User doctor = new User();
        doctor.setRole(Role.ADMIN);

        PatientRequest request = new PatientRequest();
        request.setPatientCode("P-1002");

        when(currentUserService.getCurrentUser()).thenReturn(doctor);

        assertThrows(ForbiddenException.class, () -> patientService.createPatient(request));
    }

    @Test
    void createPatient_shouldReject_whenPatientCodeAlreadyExists() {
        User doctor = new User();
        doctor.setRole(Role.DOCTOR);

        PatientRequest request = new PatientRequest();
        request.setPatientCode("P-1003");

        when(currentUserService.getCurrentUser()).thenReturn(doctor);
        when(patientRepository.existsByPatientCode("P-1003")).thenReturn(true);

        assertThrows(PatientCodeAlreadyExistsException.class, () -> patientService.createPatient(request));
    }

    @Test
    void getPatient_shouldReturnPatient_forAuthenticatedDoctor() {
        User doctor = new User();
        doctor.setId(3L);
        doctor.setRole(Role.DOCTOR);

        Patient patient = new Patient();
        patient.setId(7L);
        patient.setPatientCode("P-007");
        patient.setDoctor(doctor);

        when(currentUserService.getCurrentUser()).thenReturn(doctor);
        when(patientRepository.findByIdAndDoctor(7L, doctor)).thenReturn(Optional.of(patient));

        Patient result = patientService.getPatient(7L);

        assertEquals("P-007", result.getPatientCode());
    }

    @Test
    void updatePatient_shouldUpdateFields_whenPatientExists() {
        User doctor = new User();
        doctor.setRole(Role.DOCTOR);

        Patient patient = new Patient();
        patient.setId(8L);
        patient.setDoctor(doctor);
        patient.setFirstName("Old");
        patient.setLastName("Name");

        PatientUpdateRequest request = new PatientUpdateRequest();
        request.setFirstName("New");
        request.setLastName("Name");
        request.setDateOfBirth(LocalDate.of(1990, 5, 22));
        request.setSex(Sex.F);
        request.setAge(34);
        request.setWeight(62.0);
        request.setPhoneNumber("0600000000");

        when(currentUserService.getCurrentUser()).thenReturn(doctor);
        when(patientRepository.findByIdAndDoctor(8L, doctor)).thenReturn(Optional.of(patient));
        when(patientRepository.save(patient)).thenReturn(patient);

        Patient result = patientService.updatePatient(8L, request);

        assertEquals("New", result.getFirstName());
        assertEquals("0600000000", result.getPhoneNumber());
    }

    @Test
    void getAllPatients_shouldReturnDoctorPatients() {
        User doctor = new User();
        doctor.setRole(Role.DOCTOR);

        Patient patient = new Patient();
        patient.setId(5L);
        patient.setDoctor(doctor);

        when(currentUserService.getCurrentUser()).thenReturn(doctor);
        when(patientRepository.findAllByDoctor(doctor)).thenReturn(List.of(patient));

        List<Patient> result = patientService.getAllPatients();

        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).getId());
    }

    @Test
    void getPatient_shouldFail_whenPatientDoesNotExist() {
        User doctor = new User();
        doctor.setRole(Role.DOCTOR);

        when(currentUserService.getCurrentUser()).thenReturn(doctor);
        when(patientRepository.findByIdAndDoctor(777L, doctor)).thenReturn(Optional.empty());

        assertThrows(PatientNotFoundException.class, () -> patientService.getPatient(777L));
    }
}
