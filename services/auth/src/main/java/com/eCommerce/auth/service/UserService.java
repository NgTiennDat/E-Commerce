package com.eCommerce.auth.service;

import com.eCommerce.auth.model.request.AdminCreateUserRequest;
import com.eCommerce.auth.model.request.RegistrationRequest;
import com.eCommerce.auth.model.response.ProfileResponse;
import com.eCommerce.auth.model.response.RegistrationResponse;
import org.springframework.security.core.Authentication;

public interface UserService {

    // Public self-registration — role luôn là CUSTOMER
    RegistrationResponse register(RegistrationRequest request);

    // Admin tạo user — role do admin chỉ định
    RegistrationResponse createUser(AdminCreateUserRequest request);

    ProfileResponse getProfile(Authentication authentication);
}
