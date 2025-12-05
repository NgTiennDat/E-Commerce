package com.eCommerce.auth.service;

import com.eCommerce.auth.model.request.RegistrationRequest;
import com.eCommerce.auth.model.response.ProfileResponse;
import com.eCommerce.auth.model.response.RegistrationResponse;
import org.springframework.security.core.Authentication;

public interface UserService {
    RegistrationResponse register(RegistrationRequest request);

    ProfileResponse getProfile(Authentication authentication);
}
