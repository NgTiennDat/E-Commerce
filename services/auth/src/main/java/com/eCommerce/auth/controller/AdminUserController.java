package com.eCommerce.auth.controller;

import com.eCommerce.auth.model.request.AdminCreateUserRequest;
import com.eCommerce.auth.service.UserService;
import com.eCommerce.common.payload.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint dành riêng cho ADMIN quản lý user.
 *
 * Tại sao tách riêng khỏi UserController?
 *
 * UserController (/api/v1/user) là public-facing controller:
 *   - /register → ai cũng gọi được
 *   - /profile  → user đã login gọi
 *
 * AdminUserController (/api/v1/admin/users) là privileged controller:
 *   - Chỉ ADMIN mới được gọi
 *   - Được bảo vệ bằng RBAC permission trong DB
 *
 * Tách ra 2 controller giúp:
 *   - Rõ ràng về security boundary khi đọc code
 *   - Dễ thêm admin-only endpoints mà không làm phức tạp public controller
 *   - URL prefix /admin/ là convention rõ ràng cho ops team khi config firewall/WAF
 */
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    /**
     * Admin tạo user với role tùy chọn.
     *
     * Endpoint này được bảo vệ bằng RBAC — chỉ user có permission
     * tương ứng (POST /api/v1/admin/users) mới được gọi.
     * Permission được check tại Gateway trước khi request đến đây.
     */
    @PostMapping
    public ResponseEntity<?> createUser(
            @Valid @RequestBody AdminCreateUserRequest request
    ) {
        return ResponseEntity.ok(Response.ofSucceeded(userService.createUser(request)));
    }
}
