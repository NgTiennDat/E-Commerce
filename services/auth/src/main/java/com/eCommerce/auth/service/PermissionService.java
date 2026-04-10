package com.eCommerce.auth.service;

public interface PermissionService {

    boolean hasPermission(String username, String httpMethod, String requestPath);

    /**
     * Xóa cache permission của một user.
     * Gọi khi admin thay đổi role của user để đảm bảo
     * permission mới có hiệu lực ngay lập tức, không đợi TTL hết.
     */
    void evictUserPermissionCache(String username);
}
