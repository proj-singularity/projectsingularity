package com.projectsingularity.backend.auth.repositories;

import com.projectsingularity.backend.auth.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
}
