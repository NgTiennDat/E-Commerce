package com.eCommerce.auth.model.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Builder
public class UserResponse {

    private Long id;
    private String username;

    // thường dùng email từ UserInfo, nhưng nếu User entity cũng có email
    private String email;

    private Boolean enabled;    // hoặc status / active flag
    private Boolean deleted;    // map từ Audit.isDeleted

    private UserInfoResponse userInfo;

    private Set<RoleResponse> roles;

    // audit fields
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
