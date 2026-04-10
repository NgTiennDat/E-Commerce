package com.eCommerce.auth.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(name = "permissions",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_permission_code", columnNames = "code"),
                @UniqueConstraint(name = "UK_permission_path_method", columnNames = {"path", "http_method"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission extends Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ví dụ: PRODUCT_READ, ORDER_CREATE, USER_MANAGE
    @Column(name = "code", unique = true, nullable = false, length = 50)
    private String code;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "path", length = 200)
    private String path;

    @Column(name = "http_method", length = 10)
    private String httpMethod;

    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    private Set<Role> roles;
}