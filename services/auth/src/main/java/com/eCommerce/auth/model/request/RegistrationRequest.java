package com.eCommerce.auth.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationRequest {

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
    // roleCode đã bị xóa khỏi public registration.
    // Public endpoint luôn tạo CUSTOMER — server quyết định, không phải client.
    // Admin muốn tạo user với role khác → dùng POST /api/v1/admin/users.
}
