package tn.esprit.test.stroke_backend.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import tn.esprit.test.stroke_backend.entities.Studies;
import tn.esprit.test.stroke_backend.entities.User;

public interface StudiesRepository  extends JpaRepository<Studies, Long>{ 

    List<Studies> findAllByPatientIdAndPatientDoctor(Long patientId, User doctor);

    Optional<Studies> findByIdAndPatientDoctor(Long studyId, User doctor);

    boolean existsByStudyCode(String studyCode);
    
} 
