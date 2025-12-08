package com.eCommerce.auth.repository;

import com.eCommerce.auth.model.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PermissionRepository extends JpaRepository<Permission,Long> {
    @Query("""
        select distinct p
        from Permission p
        join p.roles r
        join r.users u
        where u.username = :username
          and upper(p.httpMethod) = upper(:method)
          and p.isDeleted = false
    """)
    List<Permission> findAllByUsernameAndMethod(String username, String method);

}
