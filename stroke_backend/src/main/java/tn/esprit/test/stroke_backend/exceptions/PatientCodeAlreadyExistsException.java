package tn.esprit.test.stroke_backend.exceptions;

public class PatientCodeAlreadyExistsException extends RuntimeException {

    public PatientCodeAlreadyExistsException(String message) {
        super(message);
    }
}