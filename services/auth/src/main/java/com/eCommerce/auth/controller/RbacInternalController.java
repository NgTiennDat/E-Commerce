package com.eCommerce.auth.controller;

import com.eCommerce.auth.common.AuthConstants;
import com.eCommerce.auth.model.request.RbacCheckRequest;
import com.eCommerce.auth.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/rbac")
@RequiredArgsConstructor
public class RbacInternalController {

    private final PermissionService permissionService;

    // Đọc secret từ config — không hardcode
    @Value("${internal.secret}")
    private String internalSecret;

    /**
     * Kiểm tra quyền RBAC cho một request.
     *
     * Endpoint này chỉ dành cho Gateway gọi nội bộ.
     * Bảo vệ bằng shared secret trong header X-Internal-Secret.
     *
     * Tại sao không dùng user JWT ở đây?
     * Vì đây là service-to-service call, không phải user call.
     * Gateway gọi endpoint này thay mặt cho user, không phải user gọi trực tiếp.
     */
    @PostMapping("/check")
    public ResponseEntity<Boolean> checkPermission(
            @RequestHeader(value = AuthConstants.INTERNAL_SECRET_HEADER, required = false) String secret,
            @RequestBody RbacCheckRequest req
    ) {
        // Validate secret trước khi làm bất kỳ điều gì khác — fail fast
        // Dùng constant-time comparison để tránh timing attack:
        // Nếu dùng .equals(), attacker có thể đo thời gian response để đoán từng ký tự của secret.
        // MessageDigest.isEqual() luôn so sánh toàn bộ byte, không dừng sớm.
        if (!isValidSecret(secret)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(false);
        }

        boolean result = permissionService.hasPermission(
                req.getUsername(), req.getMethod(), req.getPath()
        );
        return ResponseEntity.ok(result);
    }

    /**
     * So sánh secret bằng constant-time algorithm.
     *
     * Tại sao không dùng internalSecret.equals(secret)?
     *
     * String.equals() dừng so sánh ngay khi gặp ký tự khác nhau đầu tiên.
     * Điều này tạo ra sự khác biệt nhỏ về thời gian xử lý (timing difference).
     * Attacker có thể gửi hàng triệu request với các prefix khác nhau,
     * đo thời gian response, và dần dần đoán ra secret từng ký tự một.
     * Đây gọi là timing attack.
     *
     * MessageDigest.isEqual() luôn so sánh toàn bộ mảng byte dù đã tìm thấy sự khác biệt,
     * nên thời gian xử lý luôn như nhau bất kể secret đúng hay sai.
     */
    private boolean isValidSecret(String secret) {
        if (secret == null) {
            return false;
        }
        byte[] expected = internalSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] actual   = secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return java.security.MessageDigest.isEqual(expected, actual);
    }
}
