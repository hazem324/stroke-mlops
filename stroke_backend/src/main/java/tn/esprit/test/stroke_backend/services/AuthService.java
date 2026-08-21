package tn.esprit.test.stroke_backend.services;

import java.util.Locale;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;


import tn.esprit.test.stroke_backend.dto.auth.RegisterRequest;
import tn.esprit.test.stroke_backend.entities.User;
import tn.esprit.test.stroke_backend.exceptions.EmailAlreadyExistsException;
import tn.esprit.test.stroke_backend.repositories.UserRepository;
import tn.esprit.test.stroke_backend.services.servicesInterface.IAuthService;

@Service
@RequiredArgsConstructor
public class AuthService  implements IAuthService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User register(RegisterRequest request) {

        String email = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(
        "Cette adresse e-mail est déjà utilisée"
    );
        }

        // Créer le nouvel utilisateur
        User user = new User();

        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setEmail(email);

        // IMPORTANT : ne jamais enregistrer le password en clair
        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        user.setRole(request.getRole());
        user.setEstablishment(request.getEstablishment().trim());
        user.setAcceptedTerms(request.isAcceptedTerms());

        // Nouveau compte activé par défaut
        user.setEnabled(true);

        return userRepository.save(user);
    }
}