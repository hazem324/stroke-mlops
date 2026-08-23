package tn.esprit.test.stroke_backend.controller;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tn.esprit.test.stroke_backend.dto.study.StudyRequest;
import tn.esprit.test.stroke_backend.entities.Studies;
import tn.esprit.test.stroke_backend.exceptions.ForbiddenException;
import tn.esprit.test.stroke_backend.exceptions.PatientNotFoundException;
import tn.esprit.test.stroke_backend.exceptions.StudiesCodeAlreadyExistsException;
import tn.esprit.test.stroke_backend.exceptions.StudiesNotFoundException;
import tn.esprit.test.stroke_backend.services.StudiesService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class StudiesController {

    private final StudiesService studyService;

    @PostMapping("/patients/{patientId}/studies")
    public ResponseEntity<Map<String, Object>> createStudy( @PathVariable Long patientId,@Valid @RequestBody StudyRequest request) {

        try {

            Studies study =
                    studyService.createStudy(
                            patientId,
                            request
                    );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(Map.of(
                            "message",
                            "Study created successfully",

                            "studyId",
                            study.getId(),

                            "studyCode",
                            study.getStudyCode()
                    ));

        } catch (PatientNotFoundException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "message",
                            "Patient not found"
                    ));

        } catch (StudiesCodeAlreadyExistsException e) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "message",
                            "This study code already exists"
                    ));

        } catch (ForbiddenException e) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "message",
                            e.getMessage()
                    ));

        } catch (Exception e) {

            log.error(
                    "Error while creating study",
                    e
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message",
                            "Une erreur interne est survenue sur le serveur"
                    ));
        }
    }

    @GetMapping("/patients/{patientId}/studies")
    public ResponseEntity<Map<String, Object>>getPatientStudies( @PathVariable Long patientId) {

        try {

            List<Studies> studies =
                    studyService.getPatientStudies(
                            patientId
                    );

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                            "message",
                            "Studies retrieved successfully",

                            "studies",
                            studies
                    ));

        } catch (PatientNotFoundException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "message",
                            "Patient not found"
                    ));

        } catch (ForbiddenException e) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "message",
                            e.getMessage()
                    ));

        } catch (Exception e) {

            log.error(
                    "Error while retrieving patient studies",
                    e
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message",
                            "Une erreur interne est survenue sur le serveur"
                    ));
        }
    }

    @GetMapping("/studies/{studyId}")
    public ResponseEntity<Map<String, Object>>  getStudy(  @PathVariable Long studyId) {

        try {

            Studies study =
                    studyService.getStudy(
                            studyId
                    );

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                            "message",
                            "Study retrieved successfully",

                            "study",
                            study
                    ));

        } catch (StudiesNotFoundException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "message",
                            "Study not found"
                    ));

        } catch (ForbiddenException e) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "message",
                            e.getMessage()
                    ));

        } catch (Exception e) {

            log.error(
                    "Error while retrieving study",
                    e
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message",
                            "Une erreur interne est survenue sur le serveur"
                    ));
        }
    }
}