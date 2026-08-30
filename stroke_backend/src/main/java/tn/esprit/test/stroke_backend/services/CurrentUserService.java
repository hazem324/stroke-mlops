package tn.esprit.test.stroke_backend.services;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import tn.esprit.test.stroke_backend.dto.user.CurrentUserDTO;
import tn.esprit.test.stroke_backend.entities.User;
import tn.esprit.test.stroke_backend.repositories.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrentUserService {

    private final UserRepository userRepository;

    public User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String email = authentication.getName();

        log.info("AUTHENTICATED EMAIL = [{}]", email);

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Authenticated user not found: " + email
                        ));
    }

    public ResponseEntity<?> getCurrentUserC() {

        try {

            User currentUser = getCurrentUser();

            CurrentUserDTO userDTO = new CurrentUserDTO(
                    currentUser.getId(),
                    currentUser.getFirstName(),
                    currentUser.getLastName(),
                    currentUser.getEmail(),
                    currentUser.getRole() != null
                            ? currentUser.getRole().name()
                            : null,
                    currentUser.getEstablishment()
            );

            return ResponseEntity.ok(userDTO);

        } catch (Exception e) {

            log.error(
                    "Erreur lors de la récupération de l'utilisateur connecté",
                    e
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message",
                            "Impossible de récupérer les informations de l'utilisateur connecté."
                    ));
        }
    }
}