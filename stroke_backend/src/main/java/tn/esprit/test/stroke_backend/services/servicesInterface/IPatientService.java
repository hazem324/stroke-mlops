package tn.esprit.test.stroke_backend.services.servicesInterface;

import java.util.List;

import tn.esprit.test.stroke_backend.dto.patient.PatientRequest;
import tn.esprit.test.stroke_backend.dto.patient.PatientUpdateRequest;
import tn.esprit.test.stroke_backend.entities.Patient;

public interface IPatientService {

    Patient createPatient(PatientRequest patientRequest);
    Patient getPatient(Long id);
    Patient updatePatient( Long id, PatientUpdateRequest request);
    List<Patient> getAllPatients();
} 
