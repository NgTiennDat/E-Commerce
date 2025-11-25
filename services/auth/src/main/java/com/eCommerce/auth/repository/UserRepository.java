package com.eCommerce.auth.repository;

import com.eCommerce.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String username);

    @Query(nativeQuery = true, value = """
                                        SELECT IF(COUNT(*) > 0, true, false)
                                        FROM users u
                                                 INNER JOIN user_roles ur ON u.id = ur.user_id
                                                 INNER JOIN roles r ON ur.role_id = r.id
                                                 INNER JOIN role_permissions rp ON r.id = rp.role_id
                                                 INNER JOIN permissions p ON rp.permission_id = p.id
                                        WHERE u.username = :username
                                          AND u.is_deleted = false
                                          AND r.is_deleted = false
                                          AND p.is_deleted = false
                                          AND p.http_method = :httpMethod
                                          AND :path = p.path
                                        """)
    Boolean hasPermission(String username, String path, String httpMethod);
}
