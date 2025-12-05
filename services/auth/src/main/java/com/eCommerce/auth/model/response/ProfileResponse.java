package com.eCommerce.auth.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileResponse {

    private Long id;

    // login
    private String username;
    private String email;

    // info
    private String firstName;
    private String lastName;
    private String fullName;
    private String phoneNumber;
    private String address;
    private String avatarUrl;

    // trạng thái tài khoản v
    private Boolean accountNonLocked;
    private Boolean accountNonExpired;
    private Boolean enabled;

    // roles (chỉ gửi code/ name, không gửi full entity)
    private List<String> roles;

    // audit
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
