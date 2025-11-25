package com.eCommerce.auth.model.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RegistrationResponse {

    private Long userId;

    private String username;

    private String email;

    private List<String> roles;

    private String accessToken;

}
