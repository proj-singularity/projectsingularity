package com.projectsingularity.server.auth.repositories;

import com.projectsingularity.server.auth.entities.ERole;
import com.projectsingularity.server.auth.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole eRole);
}
