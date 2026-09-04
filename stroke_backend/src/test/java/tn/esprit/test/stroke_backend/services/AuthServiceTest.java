package tn.esprit.test.stroke_backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import tn.esprit.test.stroke_backend.dto.auth.LoginRequest;
import tn.esprit.test.stroke_backend.dto.auth.LoginResponse;
import tn.esprit.test.stroke_backend.dto.auth.RegisterRequest;
import tn.esprit.test.stroke_backend.entities.Role;
import tn.esprit.test.stroke_backend.entities.User;
import tn.esprit.test.stroke_backend.exceptions.AccountDisabledException;
import tn.esprit.test.stroke_backend.exceptions.EmailAlreadyExistsException;
import tn.esprit.test.stroke_backend.exceptions.InvalidCredentialsException;
import tn.esprit.test.stroke_backend.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldCreateUser_whenEmailIsAvailable() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Alice");
        request.setLastName("Martin");
        request.setEmail("alice@example.com");
        request.setPassword("Password1");
        request.setRole(Role.DOCTOR);
        request.setEstablishment("CHU Lille");
        request.setAcceptedTerms(true);

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password1")).thenReturn("encoded-password");

        User savedUser = new User();
        savedUser.setId(10L);
        savedUser.setEmail("alice@example.com");
        savedUser.setPassword("encoded-password");
        savedUser.setEnabled(true);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = authService.register(request);

        assertEquals("alice@example.com", result.getEmail());
        assertEquals("encoded-password", result.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldFail_whenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Alice");
        request.setLastName("Martin");
        request.setEmail("alice@example.com");
        request.setPassword("Password1");
        request.setRole(Role.DOCTOR);
        request.setEstablishment("CHU Lille");
        request.setAcceptedTerms(true);

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(request));
    }

    @Test
    void login_shouldReturnToken_whenCredentialsAreValid() {
        LoginRequest request = new LoginRequest();
        request.setEmail("alice@example.com");
        request.setPassword("Password1");

        User user = new User();
        user.setEmail("alice@example.com");
        user.setPassword("encoded-password");
        user.setEnabled(true);
        user.setRole(Role.DOCTOR);

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password1", "encoded-password")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        LoginResponse response = authService.login(request);

        assertEquals("Connexion réussie", response.getMessage());
        assertEquals("jwt-token", response.getAccessToken());
    }

    @Test
    void login_shouldReject_whenUserIsDisabled() {
        LoginRequest request = new LoginRequest();
        request.setEmail("alice@example.com");
        request.setPassword("Password1");

        User user = new User();
        user.setEmail("alice@example.com");
        user.setPassword("encoded-password");
        user.setEnabled(false);

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        assertThrows(AccountDisabledException.class, () -> authService.login(request));
    }

    @Test
    void login_shouldReject_whenPasswordIsIncorrect() {
        LoginRequest request = new LoginRequest();
        request.setEmail("alice@example.com");
        request.setPassword("WrongPassword1");

        User user = new User();
        user.setEmail("alice@example.com");
        user.setPassword("encoded-password");
        user.setEnabled(true);

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword1", "encoded-password")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }
}
