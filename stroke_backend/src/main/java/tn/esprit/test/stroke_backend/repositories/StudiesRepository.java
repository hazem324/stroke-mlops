package tn.esprit.test.stroke_backend.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import tn.esprit.test.stroke_backend.entities.Studies;
import tn.esprit.test.stroke_backend.entities.User;

public interface StudiesRepository extends JpaRepository<Studies, Long> {

    // Toutes les études d'un patient appartenant au médecin
    List<Studies> findAllByPatientIdAndPatientDoctor(Long patientId, User doctor);

    // Une étude appartenant à un patient du médecin
    Optional<Studies> findByIdAndPatientDoctor(Long studyId, User doctor);

    // Toutes les études d'un patient
    List<Studies> findByPatientId(Long patientId);

    // Une étude appartenant à un patient
    Optional<Studies> findByIdAndPatientId(Long studyId, Long patientId);

    // Vérifie si un studyCode existe déjà
    boolean existsByStudyCode(String studyCode);

    Optional<Studies> findById(Long id);
}