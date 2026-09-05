package tn.esprit.test.stroke_backend.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import tn.esprit.test.stroke_backend.dto.auth.LoginRequest;
import tn.esprit.test.stroke_backend.dto.auth.LoginResponse;
import tn.esprit.test.stroke_backend.dto.auth.RegisterRequest;
import tn.esprit.test.stroke_backend.entities.User;
import tn.esprit.test.stroke_backend.exceptions.AccountDisabledException;
import tn.esprit.test.stroke_backend.exceptions.EmailAlreadyExistsException;
import tn.esprit.test.stroke_backend.exceptions.InvalidCredentialsException;
import tn.esprit.test.stroke_backend.services.servicesInterface.IAuthService;

class AuthControllerCoverageTest {

    @Test
    void mapsRegistrationAndLoginOutcomes() {
        IAuthService service = mock(IAuthService.class);
        AuthController controller = new AuthController(service);
        RegisterRequest register = new RegisterRequest();
        User user = new User();
        user.setId(8L);
        user.setEmail("doctor@example.com");
        doReturn(user).when(service).register(register);
        assertEquals(202, controller.register(register).getStatusCode().value());
        doThrow(new EmailAlreadyExistsException("duplicate")).when(service).register(register);
        assertEquals(409, controller.register(register).getStatusCode().value());
        doThrow(new RuntimeException("failure")).when(service).register(register);
        assertEquals(500, controller.register(register).getStatusCode().value());

        LoginRequest login = new LoginRequest();
        doReturn(new LoginResponse("ok", "token", 10L)).when(service).login(login);
        assertEquals(200, controller.login(login).getStatusCode().value());
        doThrow(new InvalidCredentialsException("invalid")).when(service).login(login);
        assertEquals(401, controller.login(login).getStatusCode().value());
        doThrow(new AccountDisabledException("disabled")).when(service).login(login);
        assertEquals(403, controller.login(login).getStatusCode().value());
        doThrow(new RuntimeException("failure")).when(service).login(login);
        assertEquals(500, controller.login(login).getStatusCode().value());
    }

    @Test
    void exposesDebugAuthenticationDetails() {
        IAuthService service = mock(IAuthService.class);
        AuthController controller = new AuthController(service);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("doctor@example.com", "n/a"));
        assertEquals(200, controller.debugAuth().getStatusCode().value());
        assertEquals("doctor@example.com", controller.debugAuth().getBody().get("name"));
        SecurityContextHolder.clearContext();
    }
}
