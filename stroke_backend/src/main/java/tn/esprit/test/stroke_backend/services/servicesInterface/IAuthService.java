package tn.esprit.test.stroke_backend.services.servicesInterface;

import tn.esprit.test.stroke_backend.dto.auth.LoginRequest;
import tn.esprit.test.stroke_backend.dto.auth.LoginResponse;
import tn.esprit.test.stroke_backend.dto.auth.RegisterRequest;
import tn.esprit.test.stroke_backend.entities.User;

public interface IAuthService {

    User register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
} 