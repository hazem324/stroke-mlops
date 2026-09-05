package tn.esprit.test.stroke_backend.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import tn.esprit.test.stroke_backend.entities.Role;
import tn.esprit.test.stroke_backend.entities.User;
import tn.esprit.test.stroke_backend.repositories.UserRepository;
import tn.esprit.test.stroke_backend.integration.FastApiConnectionTestResponse;

@ExtendWith(MockitoExtension.class)
class CurrentUserAndDtoCoverageTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void currentUserServiceMapsAuthenticatedUserAndHandlesMissingUser() {
        UserRepository repository = mock(UserRepository.class);
        CurrentUserService service = new CurrentUserService(repository);
        User user = new User();
        user.setId(4L);
        user.setFirstName("Ada");
        user.setLastName("Lovelace");
        user.setEmail("ada@example.com");
        user.setRole(Role.DOCTOR);
        user.setEstablishment("Clinic");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("ada@example.com", "n/a"));
        doReturn(Optional.of(user)).when(repository).findByEmail("ada@example.com");
        assertSame(user, service.getCurrentUser());
        assertEquals(200, service.getCurrentUserC().getStatusCode().value());

        doReturn(Optional.empty()).when(repository).findByEmail("ada@example.com");
        assertEquals(500, service.getCurrentUserC().getStatusCode().value());
    }

    @Test
    void currentUserServiceMapsNullRole() {
        UserRepository repository = mock(UserRepository.class);
        CurrentUserService service = new CurrentUserService(repository);
        User user = new User();
        user.setEmail("user@example.com");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user@example.com", "n/a"));
        doReturn(Optional.of(user)).when(repository).findByEmail("user@example.com");
        assertEquals(200, service.getCurrentUserC().getStatusCode().value());
    }

    @Test
    void connectionResponseBeanStoresAllFields() {
        FastApiConnectionTestResponse response = new FastApiConnectionTestResponse();
        response.setStatus("success");
        response.setMessage("ok");
        response.setFilename("scan.nii.gz");
        response.setContentType("application/gzip");
        response.setSizeBytes(42L);
        assertEquals("success", response.getStatus());
        assertEquals("ok", response.getMessage());
        assertEquals("scan.nii.gz", response.getFilename());
        assertEquals("application/gzip", response.getContentType());
        assertEquals(42L, response.getSizeBytes());
        assertTrue(response.toString().contains("success"));
    }
}
