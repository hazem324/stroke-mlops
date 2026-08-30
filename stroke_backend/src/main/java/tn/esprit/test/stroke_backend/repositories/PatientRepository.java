package tn.esprit.test.stroke_backend.repositories;

import tn.esprit.test.stroke_backend.entities.Patient;
import tn.esprit.test.stroke_backend.entities.Studies;
import tn.esprit.test.stroke_backend.entities.User;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;



public interface PatientRepository extends JpaRepository<Patient, Long>{

    boolean existsByPatientCode(String patientCode);
    Optional<Patient> findByIdAndDoctor(Long id, User doctor);
     List<Patient> findAllByDoctor(User doctor);

   @Query("""
        SELECT COUNT(p)
        FROM Patient p
        WHERE p.doctor.id = :userId
    """)
    long countPatientsByDoctor(@Param("userId") Long userId);

    @Query("""
        SELECT COUNT(s)
        FROM Studies s
        WHERE s.patient.doctor.id = :userId
    """)
    long countStudiesByDoctor(@Param("userId") Long userId);

    @Query("""
        SELECT COUNT(s)
        FROM Studies s
        WHERE s.patient.doctor.id = :userId
        AND s.status = tn.esprit.test.stroke_backend.entities.StudiesStatus.COMPLETED
    """)
    long countCompletedAnalysesByDoctor(@Param("userId") Long userId);

    @Query("""
        SELECT COUNT(s)
        FROM Studies s
        WHERE s.patient.doctor.id = :userId
        AND s.status IN (
            tn.esprit.test.stroke_backend.entities.StudiesStatus.UPLOADED,
            tn.esprit.test.stroke_backend.entities.StudiesStatus.PROCESSING
        )
    """)
    long countPendingAnalysesByDoctor(@Param("userId") Long userId);

    @Query("""
    SELECT s
    FROM Studies s
    WHERE s.patient.doctor.id = :userId
    ORDER BY s.createdAt DESC
""")
List<Studies> findRecentStudiesByDoctor(
        @Param("userId") Long userId,
        Pageable pageable
);

}
