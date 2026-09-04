package tn.esprit.test.stroke_backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import tn.esprit.test.stroke_backend.dto.auth.LoginRequest;
import tn.esprit.test.stroke_backend.dto.auth.LoginResponse;
import tn.esprit.test.stroke_backend.dto.auth.RegisterRequest;
import tn.esprit.test.stroke_backend.entities.Role;
import tn.esprit.test.stroke_backend.entities.User;
import tn.esprit.test.stroke_backend.exceptions.AccountDisabledException;
import tn.esprit.test.stroke_backend.exceptions.EmailAlreadyExistsException;
import tn.esprit.test.stroke_backend.exceptions.InvalidCredentialsException;
import tn.esprit.test.stroke_backend.services.servicesInterface.IAuthService;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IAuthService authService;

    @Test
    void register_shouldReturnCreated_whenRequestIsValid() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Alice");
        request.setLastName("Martin");
        request.setEmail("alice@example.com");
        request.setPassword("Password1");
        request.setRole(Role.DOCTOR);
        request.setEstablishment("CHU Lille");
        request.setAcceptedTerms(true);

        User user = new User();
        user.setId(42L);
        user.setEmail("alice@example.com");

        when(authService.register(any(RegisterRequest.class))).thenReturn(user);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.message").value("Compte créé avec succès"))
            .andExpect(jsonPath("$.userId").value(42));
    }

    @Test
    void register_shouldReturnConflict_whenEmailExists() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Alice");
        request.setLastName("Martin");
        request.setEmail("alice@example.com");
        request.setPassword("Password1");
        request.setRole(Role.DOCTOR);
        request.setEstablishment("CHU Lille");
        request.setAcceptedTerms(true);

        when(authService.register(any(RegisterRequest.class)))
            .thenThrow(new EmailAlreadyExistsException("Cette adresse e-mail est déjà utilisée"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("Cette adresse e-mail est déjà utilisée"));
    }

    @Test
    void login_shouldReturnToken_whenCredentialsAreValid() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("alice@example.com");
        request.setPassword("Password1");

        LoginResponse response = new LoginResponse("Connexion réussie", "jwt-token", 3600L);
        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Connexion réussie"))
            .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void login_shouldReturnUnauthorized_whenCredentialsAreInvalid() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("alice@example.com");
        request.setPassword("WrongPassword1");

        when(authService.login(any(LoginRequest.class)))
            .thenThrow(new InvalidCredentialsException("Email ou mot de passe incorrect"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Email ou mot de passe incorrect"));
    }

    @Test
    void login_shouldReturnForbidden_whenAccountIsDisabled() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("alice@example.com");
        request.setPassword("Password1");

        when(authService.login(any(LoginRequest.class)))
            .thenThrow(new AccountDisabledException("Votre compte est désactivé"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message").value("Votre compte est désactivé"));
    }
}
