package tn.esprit.test.stroke_backend.services;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import tn.esprit.test.stroke_backend.dto.study.StudyRequest;
import tn.esprit.test.stroke_backend.entities.Patient;
import tn.esprit.test.stroke_backend.entities.Role;
import tn.esprit.test.stroke_backend.entities.Studies;
import tn.esprit.test.stroke_backend.entities.User;
import tn.esprit.test.stroke_backend.exceptions.ForbiddenException;
import tn.esprit.test.stroke_backend.exceptions.PatientNotFoundException;
import tn.esprit.test.stroke_backend.exceptions.StudiesCodeAlreadyExistsException;
import tn.esprit.test.stroke_backend.exceptions.StudiesNotFoundException;
import tn.esprit.test.stroke_backend.repositories.PatientRepository;
import tn.esprit.test.stroke_backend.repositories.StudiesRepository;
import tn.esprit.test.stroke_backend.services.servicesInterface.IStudiesService;

@Service
@RequiredArgsConstructor
public class StudiesService implements IStudiesService {

    private final StudiesRepository studyRepository;
    private final PatientRepository patientRepository;
    private final CurrentUserService currentUserService;

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

        if (studyRepository
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

        return studyRepository.save(study);
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

        return studyRepository
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

        return studyRepository
                .findByIdAndPatientDoctor(
                        studyId,
                        doctor
                )
                .orElseThrow(() ->
                        new StudiesNotFoundException(
                                "Study not found"
                        ));
    }
}