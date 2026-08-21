package tn.esprit.test.stroke_backend.controller;

import java.util.HashMap;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import tn.esprit.test.stroke_backend.dto.auth.RegisterRequest;
import tn.esprit.test.stroke_backend.entities.User;
import tn.esprit.test.stroke_backend.services.servicesInterface.IAuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(
            @Valid @RequestBody RegisterRequest request) {

        User user = authService.register(request);

        Map<String, Object> response = new HashMap<>();

        response.put("message", "Compte créé avec succès");
        response.put("userId", user.getId());
        response.put("email", user.getEmail());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}