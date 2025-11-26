package com.eCommerce.auth.repository;

import com.eCommerce.auth.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByCode(String defaultRoleCode);
}
