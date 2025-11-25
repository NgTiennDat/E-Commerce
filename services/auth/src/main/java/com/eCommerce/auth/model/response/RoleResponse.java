package com.eCommerce.auth.model.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Builder
public class RoleResponse {

    private Long id;
    private String code;        // ADMIN, SELLER, CUSTOMER, ...
    private String name;
    private String description;

    private Set<PermissionResponse> permissions;
}
