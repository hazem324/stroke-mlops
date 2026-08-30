package tn.esprit.test.stroke_backend.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUserDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private String establishment;
}