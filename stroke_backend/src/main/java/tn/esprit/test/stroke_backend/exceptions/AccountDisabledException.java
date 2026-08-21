package tn.esprit.test.stroke_backend.exceptions;

public class AccountDisabledException extends RuntimeException {

    public AccountDisabledException(String message) {
        super(message);
    }
}