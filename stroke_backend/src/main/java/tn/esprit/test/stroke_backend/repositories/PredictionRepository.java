package tn.esprit.test.stroke_backend.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import tn.esprit.test.stroke_backend.entities.Prediction;

public interface PredictionRepository  extends JpaRepository<Prediction, Long>{
    
    // Récupère la prédiction d'une étude.
    Optional<Prediction> findByStudyId(Long studyId);

    // Vérifie si une prédiction existe déjà pour une étude.
    boolean existsByStudyId(Long studyId);
}
