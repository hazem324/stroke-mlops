package tn.esprit.test.stroke_backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import tn.esprit.test.stroke_backend.StrokeBackendApplication;
import tn.esprit.test.stroke_backend.dto.patient.PatientRequest;
import tn.esprit.test.stroke_backend.dto.patient.PatientUpdateRequest;
import tn.esprit.test.stroke_backend.entities.Patient;
import tn.esprit.test.stroke_backend.entities.Sex;
import tn.esprit.test.stroke_backend.exceptions.ForbiddenException;
import tn.esprit.test.stroke_backend.exceptions.PatientNotFoundException;
import tn.esprit.test.stroke_backend.services.PatientService;

@SpringBootTest(classes = StrokeBackendApplication.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private PatientService patientService;

    @Test
    void createPatient_shouldReturnCreated_whenDataIsValid() throws Exception {
        PatientRequest request = new PatientRequest();
        request.setPatientCode("P-1001");
        request.setFirstName("Jean");
        request.setLastName("Dupont");
        request.setDateOfBirth(LocalDate.of(1980, 1, 10));
        request.setSex(Sex.M);
        request.setAge(45);
        request.setWeight(75.5);
        request.setPhoneNumber("0612345678");

        Patient patient = new Patient();
        patient.setId(12L);
        patient.setPatientCode("P-1001");

        when(patientService.createPatient(any(PatientRequest.class))).thenReturn(patient);

        mockMvc.perform(post("/api/patient")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.message").value("Patient créé avec succès"))
            .andExpect(jsonPath("$.patientId").value(12));
    }

    @Test
    void getPatient_shouldReturnNotFound_whenPatientDoesNotExist() throws Exception {
        when(patientService.getPatient(999L)).thenThrow(new PatientNotFoundException("Patient not found"));

        mockMvc.perform(get("/api/patient/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Patient not found"));
    }

    @Test
    void updatePatient_shouldReturnForbidden_whenUserIsNotDoctor() throws Exception {
        PatientUpdateRequest request = new PatientUpdateRequest();
        request.setFirstName("Jean");
        request.setLastName("Dupont");
        request.setDateOfBirth(LocalDate.of(1980, 1, 1));
        request.setSex(Sex.M);
        request.setAge(40);
        request.setWeight(70.0);
        request.setPhoneNumber("0600000000");

        when(patientService.updatePatient(1L, any(PatientUpdateRequest.class)))
            .thenThrow(new ForbiddenException("Only a doctor can update patients"));

        mockMvc.perform(put("/api/patient/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message").value("Only a doctor can update patients"));
    }
}
