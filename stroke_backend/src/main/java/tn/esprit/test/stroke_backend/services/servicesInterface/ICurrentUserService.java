package tn.esprit.test.stroke_backend.services.servicesInterface;

import org.springframework.http.ResponseEntity;

import tn.esprit.test.stroke_backend.entities.User;

public interface ICurrentUserService {
    
     User getCurrentUser();
     ResponseEntity<?> getCurrentUserC();
}
