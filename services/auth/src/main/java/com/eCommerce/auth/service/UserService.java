package com.eCommerce.auth.service;

import com.eCommerce.auth.model.request.RegistrationRequest;
import com.eCommerce.auth.model.response.RegistrationResponse;

public interface UserService {
    RegistrationResponse register(RegistrationRequest request);
}
