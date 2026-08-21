package tn.esprit.test.stroke_backend.repositories;

import tn.esprit.test.stroke_backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepositorie extends JpaRepository<User, Long> {

}