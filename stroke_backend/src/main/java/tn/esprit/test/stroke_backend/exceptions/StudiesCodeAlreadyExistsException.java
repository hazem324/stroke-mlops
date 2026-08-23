package tn.esprit.test.stroke_backend.exceptions;

public class StudiesCodeAlreadyExistsException
        extends RuntimeException {

    public StudiesCodeAlreadyExistsException(String message) {
        super(message);
    }
}