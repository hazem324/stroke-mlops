package tn.esprit.test.stroke_backend.controller;

import java.util.Map;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tn.esprit.test.stroke_backend.dto.auth.RegisterRequest;
import tn.esprit.test.stroke_backend.entities.User;
import tn.esprit.test.stroke_backend.exceptions.EmailAlreadyExistsException;
import tn.esprit.test.stroke_backend.services.servicesInterface.IAuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(
            @Valid @RequestBody RegisterRequest request) {

        try {

            User user = authService.register(request);

            return ResponseEntity
                    .status(HttpStatus.ACCEPTED)
                    .body(Map.of(
                            "message", "Compte créé avec succès",
                            "userId", user.getId(),
                            "email", user.getEmail()
                    ));

        } catch (EmailAlreadyExistsException e) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "message", "Cette adresse e-mail est déjà utilisée"
                    ));

        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Une erreur interne est survenue sur le serveur"
                    ));
        }
    }
}