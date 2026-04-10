package com.eCommerce.auth.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request để ADMIN tạo user với role tùy chọn.
 *
 * Tại sao tách riêng thay vì tái dùng RegistrationRequest?
 *
 * RegistrationRequest là public contract — ai cũng gửi được.
 * AdminCreateUserRequest là privileged contract — chỉ ADMIN mới gọi được endpoint này.
 * 2 contract khác nhau về security context nên tách thành 2 class riêng:
 *   - Dễ thêm field riêng cho từng use case mà không ảnh hưởng nhau
 *   - Validation rule có thể khác nhau (ví dụ: admin không cần gửi password,
 *     hệ thống tự generate và gửi email)
 *   - Rõ ràng về intent khi đọc code
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminCreateUserRequest {

    @NotBlank
    @Size(min = 4, max = 50)
    private String username;

    @NotBlank
    @Email
    @Size(max = 150)
    private String email;

    @NotBlank
    @Size(min = 6, max = 100)
    private String password;

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Size(max = 20)
    private String phoneNumber;

    @Size(max = 255)
    private String avatarUrl;

    @Size(max = 255)
    private String address;

    // Admin được phép chỉ định role — đây là điểm khác biệt cốt lõi
    // so với RegistrationRequest (public) không có field này.
    @NotBlank(message = "Role code is required")
    private String roleCode;
}
