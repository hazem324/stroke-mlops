package tn.esprit.test.stroke_backend.services;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import tn.esprit.test.stroke_backend.dto.patient.PatientRequest;
import tn.esprit.test.stroke_backend.dto.patient.PatientUpdateRequest;
import tn.esprit.test.stroke_backend.entities.Patient;
import tn.esprit.test.stroke_backend.entities.Role;
import tn.esprit.test.stroke_backend.entities.User;
import tn.esprit.test.stroke_backend.exceptions.ForbiddenException;
import tn.esprit.test.stroke_backend.exceptions.PatientCodeAlreadyExistsException;
import tn.esprit.test.stroke_backend.exceptions.PatientNotFoundException;
import tn.esprit.test.stroke_backend.repositories.PatientRepository;
import tn.esprit.test.stroke_backend.services.servicesInterface.IPatientService;

@Service
@RequiredArgsConstructor
public class PatientService implements IPatientService {

    private final PatientRepository patientRepository;
    private final CurrentUserService currentUserService;


    // CREATE PATIENT
    @Override
    public Patient createPatient(PatientRequest request) {

        User doctor = currentUserService.getCurrentUser();

        // Check role
        if (doctor.getRole() != Role.DOCTOR) {
            throw new ForbiddenException(
                    "Only a doctor can create a patient"
            );
        }

        // Check duplicate patient code
        if (patientRepository.existsByPatientCode(
                request.getPatientCode())) {

            throw new PatientCodeAlreadyExistsException(
                    "Patient code already exists"
            );
        }

        Patient patient = new Patient();

        patient.setPatientCode(request.getPatientCode());
        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setSex(request.getSex());
        patient.setAge(request.getAge());
        patient.setWeight(request.getWeight());
        patient.setPhoneNumber(request.getPhoneNumber());

        // IMPORTANT:
        // Doctor comes from JWT, not from frontend
        patient.setDoctor(doctor);

        return patientRepository.save(patient);
    }

    // GET PATIENT
    @Override
    public Patient getPatient(Long id) {

        User doctor = currentUserService.getCurrentUser();

        // Check role
        if (doctor.getRole() != Role.DOCTOR) {
            throw new ForbiddenException(
                    "Only a doctor can access patients"
            );
        }

        return patientRepository
                .findByIdAndDoctor(id, doctor)
                .orElseThrow(() ->
                        new PatientNotFoundException(
                                "Patient not found"
                        )
                );
    }

    // UPDATE PATIENT
    @Override
    public Patient updatePatient(
            Long id,
            PatientUpdateRequest request) {

        User doctor = currentUserService.getCurrentUser();

        // Check role
        if (doctor.getRole() != Role.DOCTOR) {
            throw new ForbiddenException(
                    "Only a doctor can update patients"
            );
        }

        Patient patient = patientRepository
                .findByIdAndDoctor(id, doctor)
                .orElseThrow(() ->
                        new PatientNotFoundException(
                                "Patient not found"
                        )
                );

        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setSex(request.getSex());
        patient.setAge(request.getAge());
        patient.setWeight(request.getWeight());
        patient.setPhoneNumber(request.getPhoneNumber());

        return patientRepository.save(patient);
    }
     
    public List<Patient> getAllPatients() {

    User doctor = currentUserService.getCurrentUser();

    if (doctor.getRole() != Role.DOCTOR) {
        throw new ForbiddenException(
                "Only a doctor can access patients"
        );
    }

    return patientRepository.findAllByDoctor(doctor);
}

}