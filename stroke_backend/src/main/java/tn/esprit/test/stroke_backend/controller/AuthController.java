package tn.esprit.test.stroke_backend.controller;

import java.util.Map;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import tn.esprit.test.stroke_backend.dto.auth.LoginResponse;
import tn.esprit.test.stroke_backend.dto.auth.LoginRequest;
import tn.esprit.test.stroke_backend.dto.auth.RegisterRequest;
import tn.esprit.test.stroke_backend.entities.User;
import tn.esprit.test.stroke_backend.exceptions.AccountDisabledException;
import tn.esprit.test.stroke_backend.exceptions.EmailAlreadyExistsException;
import tn.esprit.test.stroke_backend.exceptions.InvalidCredentialsException;
import tn.esprit.test.stroke_backend.services.servicesInterface.IAuthService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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


    @PostMapping("/login")
public ResponseEntity<Map<String, Object>> login(
        @Valid @RequestBody LoginRequest request) {

    try {

        LoginResponse response = authService.login(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of(
                        "message", response.getMessage(),
                        "token", response.getAccessToken()
                ));

    } catch (InvalidCredentialsException e) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "message",
                        "Email ou mot de passe incorrect"
                ));

    } catch (AccountDisabledException e) {

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Map.of(
                        "message",
                        "Votre compte est désactivé"
                ));

    } catch (Exception e) {

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "message",
                        "Une erreur interne est survenue sur le serveur"
                ));
    }
}



@GetMapping("/debug-auth")
public ResponseEntity<Map<String, Object>> debugAuth() {

    Authentication authentication =
            SecurityContextHolder
                    .getContext()
                    .getAuthentication();

    return ResponseEntity.ok(
            Map.of(
                    "authenticationClass",
                    authentication.getClass().getName(),

                    "principalClass",
                    authentication.getPrincipal()
                            .getClass().getName(),

                    "name",
                    authentication.getName(),

                    "authenticated",
                    authentication.isAuthenticated(),

                    "authorities",
                    authentication.getAuthorities()
            )
    );
}


}