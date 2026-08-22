package tn.esprit.test.stroke_backend.controller;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tn.esprit.test.stroke_backend.dto.patient.PatientRequest;
import tn.esprit.test.stroke_backend.dto.patient.PatientUpdateRequest;
import tn.esprit.test.stroke_backend.entities.Patient;
import tn.esprit.test.stroke_backend.exceptions.ForbiddenException;
import tn.esprit.test.stroke_backend.exceptions.PatientCodeAlreadyExistsException;
import tn.esprit.test.stroke_backend.exceptions.PatientNotFoundException;
import tn.esprit.test.stroke_backend.services.PatientService;

@RestController
@RequestMapping("/api/patient")
@RequiredArgsConstructor
@Slf4j
public class PatientController {


    private final PatientService patientService;

    // CREATE
    @PostMapping
    public ResponseEntity<Map<String, Object>> createPatient(
            @Valid @RequestBody PatientRequest request) {

        try {

            Patient patient =
                    patientService.createPatient(request);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(Map.of(
                            "message",
                            "Patient créé avec succès",

                            "patientId",
                            patient.getId(),

                            "patientCode",
                            patient.getPatientCode()
                    ));

        } catch (PatientCodeAlreadyExistsException e) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "message",
                            "Ce code patient existe déjà"
                    ));

        } catch (ForbiddenException e) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "message",
                            e.getMessage()
                    ));

        } catch (Exception e) {

            // DEBUG
            log.error(
                    "Error while creating patient",
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

    // GET
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getPatient(
            @PathVariable Long id) {

        try {

            Patient patient =
                    patientService.getPatient(id);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                            "patient",
                            patient
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

            // DEBUG
            log.error(
                    "Error while retrieving patient {}",
                    id,
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

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody PatientUpdateRequest request) {

        try {

            Patient patient =
                    patientService.updatePatient(
                            id,
                            request
                    );

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                            "message",
                            "Patient updated successfully",

                            "patientId",
                            patient.getId(),

                            "patientCode",
                            patient.getPatientCode()
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

            // DEBUG
            log.error(
                    "Error while updating patient {}",
                    id,
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

    @GetMapping("/by-docktor")
    public ResponseEntity<Map<String, Object>> getAllPatients() {

    try {

        List<Patient> patients =
                patientService.getAllPatients();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of(
                        "patients", patients
                ));

    } catch (ForbiddenException e) {

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Map.of(
                        "message", e.getMessage()
                ));

    } catch (Exception e) {

        log.error(
                "Error while retrieving patients",
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