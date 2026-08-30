package tn.esprit.test.stroke_backend.services;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import tn.esprit.test.stroke_backend.dto.statistics.DashboardStatisticsDTO;
import tn.esprit.test.stroke_backend.dto.statistics.RecentAnalysisDTO;
import tn.esprit.test.stroke_backend.entities.Studies;
import tn.esprit.test.stroke_backend.entities.User;
import tn.esprit.test.stroke_backend.repositories.PatientRepository;
import tn.esprit.test.stroke_backend.services.servicesInterface.IDashboardService;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
@RequiredArgsConstructor
public class DashboardService implements IDashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);

     private final PatientRepository patientRepository;
    private final CurrentUserService currentUserService;

    public ResponseEntity<?> getStatistics() {
        try {
            User currentUser = currentUserService.getCurrentUser();
            Long userId = currentUser.getId();

            DashboardStatisticsDTO stats = new DashboardStatisticsDTO(
                    patientRepository.countPatientsByDoctor(userId),
                    patientRepository.countStudiesByDoctor(userId),
                    patientRepository.countCompletedAnalysesByDoctor(userId),
                    patientRepository.countPendingAnalysesByDoctor(userId)
            );

            return ResponseEntity.status(HttpStatus.OK).body(stats);

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des statistiques du dashboard", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erreur lors de la récupération des statistiques."));
        }
    }

    public ResponseEntity<?> getRecentAnalyses() {
        try {
            User currentUser = currentUserService.getCurrentUser();
            Long userId = currentUser.getId();
            Pageable pageable = PageRequest.of(0, 5);

            List<Studies> studies = patientRepository.findRecentStudiesByDoctor(userId, pageable);

            List<RecentAnalysisDTO> analyses = studies.stream()
                    .map(study -> new RecentAnalysisDTO(
                            study.getId(),
                            study.getPatient().getId(),
                            study.getPatient().getPatientCode(),
                            study.getPatient().getFirstName() + " " + study.getPatient().getLastName(),
                            study.getStudyCode(),
                            study.getStudyDate(),
                            study.getModality() != null ? study.getModality().name() : null,
                            study.getStatus() != null ? study.getStatus().name() : null,
                            study.getPrediction() != null ? study.getPrediction().getLesionDetected() : null,
                            study.getCreatedAt()
                    ))
                    .toList();

            return ResponseEntity.status(HttpStatus.OK).body(analyses);

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des analyses récentes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erreur lors de la récupération des analyses récentes."));
        }
    }
}