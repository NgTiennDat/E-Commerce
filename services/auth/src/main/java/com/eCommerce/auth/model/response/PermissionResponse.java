package com.eCommerce.auth.model.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PermissionResponse {

    private Long id;
    private String code;
    private String name;
    private String description;
    private String path;
    private String httpMethod;
}
