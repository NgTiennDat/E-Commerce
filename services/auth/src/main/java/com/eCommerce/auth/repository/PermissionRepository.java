package com.eCommerce.auth.repository;

import com.eCommerce.auth.model.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission,Long> {
}
