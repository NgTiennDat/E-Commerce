package com.eCommerce.auth.service;

import com.eCommerce.auth.model.entity.User;

/**
 * Quản lý vòng đời của refresh token.
 *
 * Storage strategy: DB là source of truth, Redis là cache/index.
 *   - DB (refresh_token table): lưu toàn bộ token, hỗ trợ audit + revocation
 *   - Redis (auth:rt:token:{token}): index nhanh để validate mà không cần DB
 *   - Redis (auth:rt:blacklist:{token}): đánh dấu token đã bị revoke
 *
 * Multi-device support: mỗi login tạo 1 token riêng, không ghi đè token cũ.
 * User có thể login từ nhiều thiết bị đồng thời.
 * Logout 1 thiết bị chỉ revoke token của thiết bị đó.
 */
public interface RefreshTokenService {

    /**
     * Tạo refresh token mới cho user, lưu vào DB + Redis.
     * Gọi sau khi login hoặc refresh thành công.
     *
     * @return JWT refresh token string
     */
    String create(User user);

    /**
     * Validate refresh token còn hợp lệ không.
     * Check theo thứ tự: blacklist → Redis → DB (fallback).
     *
     * @return true nếu token hợp lệ và chưa bị revoke
     */
    boolean validate(String token);

    /**
     * Revoke 1 refresh token cụ thể.
     * Dùng khi user logout từ 1 thiết bị.
     */
    void revoke(String token);

    /**
     * Revoke tất cả refresh token của 1 user.
     * Dùng khi user đổi password hoặc admin force logout.
     */
    void revokeAll(Long userId);
}
