package tn.esprit.test.stroke_backend.controller;

import lombok.RequiredArgsConstructor;
import tn.esprit.test.stroke_backend.services.CurrentUserService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    
    private final CurrentUserService currentUserService;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        return currentUserService.getCurrentUserC();
    }

}
