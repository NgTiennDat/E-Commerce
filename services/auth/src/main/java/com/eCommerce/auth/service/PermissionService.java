package com.eCommerce.auth.service;

public interface PermissionService {

    boolean hasPermission(String username, String httpMethod, String requestPath);
}
