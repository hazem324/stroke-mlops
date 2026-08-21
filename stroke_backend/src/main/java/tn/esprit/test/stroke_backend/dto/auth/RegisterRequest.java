package tn.esprit.test.stroke_backend.dto.auth;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import tn.esprit.test.stroke_backend.entities.Role;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterRequest {

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 100, message = "Le prénom ne doit pas dépasser 100 caractères")
    String firstName;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    String lastName;

    @NotBlank(message = "L'adresse e-mail est obligatoire")
    @Email(message = "L'adresse e-mail n'est pas valide")
    @Size(max = 255, message = "L'adresse e-mail est trop longue")
    String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, max = 100,
          message = "Le mot de passe doit contenir entre 8 et 100 caractères")
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*\\d).*$",
        message = "Le mot de passe doit contenir au moins une majuscule et un chiffre"
    )
    String password;

    @NotNull(message = "Le rôle est obligatoire")
    Role role;

    @NotBlank(message = "L'établissement est obligatoire")
    @Size(max = 255, message = "L'établissement est trop long")
    String establishment;

    @AssertTrue(message = "Vous devez accepter les conditions d'utilisation")
    boolean acceptedTerms;
}