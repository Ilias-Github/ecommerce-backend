package com.ecommerce.project.repositories;

import com.ecommerce.project.model.ERole;
import com.ecommerce.project.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IRoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(ERole eRole);
}
