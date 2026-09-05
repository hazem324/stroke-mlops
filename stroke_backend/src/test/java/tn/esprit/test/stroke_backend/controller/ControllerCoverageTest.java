package tn.esprit.test.stroke_backend.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import tn.esprit.test.stroke_backend.entities.Patient;
import tn.esprit.test.stroke_backend.entities.Studies;
import tn.esprit.test.stroke_backend.entities.StudiesStatus;
import tn.esprit.test.stroke_backend.exceptions.ForbiddenException;
import tn.esprit.test.stroke_backend.exceptions.PatientCodeAlreadyExistsException;
import tn.esprit.test.stroke_backend.exceptions.PatientNotFoundException;
import tn.esprit.test.stroke_backend.exceptions.StudiesCodeAlreadyExistsException;
import tn.esprit.test.stroke_backend.exceptions.StudiesNotFoundException;
import tn.esprit.test.stroke_backend.services.PatientService;
import tn.esprit.test.stroke_backend.services.StudiesService;

class ControllerCoverageTest {

    @Test
    void patientControllerCoversSuccessAndMappedErrors() {
        PatientService service = mock(PatientService.class);
        PatientController controller = new PatientController(service);
        Patient patient = new Patient();
        patient.setId(1L);
        patient.setPatientCode("P1");
        var request = new tn.esprit.test.stroke_backend.dto.patient.PatientRequest();
        doReturn(patient).when(service).createPatient(request);
        assertEquals(201, controller.createPatient(request).getStatusCode().value());
        doThrow(new PatientCodeAlreadyExistsException("duplicate")).when(service).createPatient(request);
        assertEquals(409, controller.createPatient(request).getStatusCode().value());
        doThrow(new ForbiddenException("denied")).when(service).createPatient(request);
        assertEquals(403, controller.createPatient(request).getStatusCode().value());
        doThrow(new RuntimeException("failure")).when(service).createPatient(request);
        assertEquals(500, controller.createPatient(request).getStatusCode().value());

        doReturn(patient).when(service).getPatient(1L);
        assertEquals(200, controller.getPatient(1L).getStatusCode().value());
        doThrow(new PatientNotFoundException("missing")).when(service).getPatient(1L);
        assertEquals(404, controller.getPatient(1L).getStatusCode().value());
        doThrow(new ForbiddenException("denied")).when(service).getPatient(1L);
        assertEquals(403, controller.getPatient(1L).getStatusCode().value());
        doThrow(new RuntimeException("failure")).when(service).getPatient(1L);
        assertEquals(500, controller.getPatient(1L).getStatusCode().value());

        var update = new tn.esprit.test.stroke_backend.dto.patient.PatientUpdateRequest();
        doReturn(patient).when(service).updatePatient(1L, update);
        assertEquals(200, controller.updatePatient(1L, update).getStatusCode().value());
        doThrow(new PatientNotFoundException("missing")).when(service).updatePatient(1L, update);
        assertEquals(404, controller.updatePatient(1L, update).getStatusCode().value());
        doThrow(new ForbiddenException("denied")).when(service).updatePatient(1L, update);
        assertEquals(403, controller.updatePatient(1L, update).getStatusCode().value());
        doThrow(new RuntimeException("failure")).when(service).updatePatient(1L, update);
        assertEquals(500, controller.updatePatient(1L, update).getStatusCode().value());

        doReturn(List.of(patient)).when(service).getAllPatients();
        assertEquals(200, controller.getAllPatients().getStatusCode().value());
        doThrow(new ForbiddenException("denied")).when(service).getAllPatients();
        assertEquals(403, controller.getAllPatients().getStatusCode().value());
        doThrow(new RuntimeException("failure")).when(service).getAllPatients();
        assertEquals(500, controller.getAllPatients().getStatusCode().value());
    }

    @Test
    void studiesControllerCoversCrudAndAnalysisMappings() {
        StudiesService service = mock(StudiesService.class);
        StudiesController controller = new StudiesController(service);
        Studies study = new Studies();
        study.setId(2L);
        study.setStudyCode("S2");
        var request = new tn.esprit.test.stroke_backend.dto.study.StudyRequest();
        doReturn(study).when(service).createStudy(1L, request);
        assertEquals(201, controller.createStudy(1L, request).getStatusCode().value());
        doThrow(new PatientNotFoundException("missing")).when(service).createStudy(1L, request);
        assertEquals(404, controller.createStudy(1L, request).getStatusCode().value());
        doThrow(new StudiesCodeAlreadyExistsException("duplicate")).when(service).createStudy(1L, request);
        assertEquals(409, controller.createStudy(1L, request).getStatusCode().value());
        doThrow(new ForbiddenException("denied")).when(service).createStudy(1L, request);
        assertEquals(403, controller.createStudy(1L, request).getStatusCode().value());
        doThrow(new RuntimeException("failure")).when(service).createStudy(1L, request);
        assertEquals(500, controller.createStudy(1L, request).getStatusCode().value());

        doReturn(List.of(study)).when(service).getPatientStudies(1L);
        assertEquals(200, controller.getPatientStudies(1L).getStatusCode().value());
        doThrow(new PatientNotFoundException("missing")).when(service).getPatientStudies(1L);
        assertEquals(404, controller.getPatientStudies(1L).getStatusCode().value());
        doThrow(new ForbiddenException("denied")).when(service).getPatientStudies(1L);
        assertEquals(403, controller.getPatientStudies(1L).getStatusCode().value());
        doThrow(new RuntimeException("failure")).when(service).getPatientStudies(1L);
        assertEquals(500, controller.getPatientStudies(1L).getStatusCode().value());

        doReturn(study).when(service).getStudy(2L);
        assertEquals(200, controller.getStudy(2L).getStatusCode().value());
        doThrow(new StudiesNotFoundException("missing")).when(service).getStudy(2L);
        assertEquals(404, controller.getStudy(2L).getStatusCode().value());
        doThrow(new ForbiddenException("denied")).when(service).getStudy(2L);
        assertEquals(403, controller.getStudy(2L).getStatusCode().value());
        doThrow(new RuntimeException("failure")).when(service).getStudy(2L);
        assertEquals(500, controller.getStudy(2L).getStatusCode().value());

        var response = new tn.esprit.test.stroke_backend.dto.study.StudyResponseDTO();
        response.setStatus(StudiesStatus.COMPLETED);
        doReturn(response).when(service).analyzeStudy(anyLong(), any(), any());
        assertEquals(200, controller.analyzeStudy(1L, new MockMultipartFile("file", "x.nii.gz", "application/gzip", new byte[] {1}), tn.esprit.test.stroke_backend.entities.Modality.DWI).getStatusCode().value());
        response.setStatus(StudiesStatus.FAILED);
        assertEquals(422, controller.analyzeStudy(1L, new MockMultipartFile("file", "x.nii.gz", "application/gzip", new byte[] {1}), tn.esprit.test.stroke_backend.entities.Modality.DWI).getStatusCode().value());
        doThrow(new PatientNotFoundException("missing")).when(service).analyzeStudy(anyLong(), any(), any());
        assertEquals(404, controller.analyzeStudy(1L, null, null).getStatusCode().value());
        doThrow(new ForbiddenException("denied")).when(service).analyzeStudy(anyLong(), any(), any());
        assertEquals(403, controller.analyzeStudy(1L, null, null).getStatusCode().value());
        doThrow(new IllegalArgumentException("bad")).when(service).analyzeStudy(anyLong(), any(), any());
        assertEquals(400, controller.analyzeStudy(1L, null, null).getStatusCode().value());
        doThrow(new RuntimeException("failure")).when(service).analyzeStudy(anyLong(), any(), any());
        assertEquals(500, controller.analyzeStudy(1L, null, null).getStatusCode().value());
    }
}
