package tn.esprit.test.stroke_backend.repositories;

import tn.esprit.test.stroke_backend.entities.Patient;
import tn.esprit.test.stroke_backend.entities.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;


public interface PatientRepository extends JpaRepository<Patient, Long>{

    boolean existsByPatientCode(String patientCode);

    Optional<Patient> findByIdAndDoctor(Long id, User doctor);

    
} 