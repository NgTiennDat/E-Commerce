package com.eCommerce.auth.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RbacCheckRequest {
    private String username;
    private String method;
    private String path;
}

