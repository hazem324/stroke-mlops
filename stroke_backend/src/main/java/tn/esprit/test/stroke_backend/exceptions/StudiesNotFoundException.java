package tn.esprit.test.stroke_backend.exceptions;

public class StudiesNotFoundException
        extends RuntimeException {

    public StudiesNotFoundException(String message) {
        super(message);
    }
}