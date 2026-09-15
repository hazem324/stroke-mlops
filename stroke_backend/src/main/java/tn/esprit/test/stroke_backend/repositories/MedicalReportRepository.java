package tn.esprit.test.stroke_backend.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import tn.esprit.test.stroke_backend.entities.MedicalReport;
import tn.esprit.test.stroke_backend.entities.Studies;


public interface MedicalReportRepository extends JpaRepository<MedicalReport, Long>{

    Optional<MedicalReport> findByStudy(Studies study);
    
} 