package com.eCommerce.auth.service.impl;

import com.eCommerce.auth.entity.Role;
import com.eCommerce.auth.entity.User;
import com.eCommerce.auth.model.request.RegistrationRequest;
import com.eCommerce.auth.model.response.RegistrationResponse;
import com.eCommerce.auth.repository.RoleRepository;
import com.eCommerce.auth.repository.UserRepository;
import com.eCommerce.auth.service.AuthService;
import com.eCommerce.auth.utils.JwtUtils;
import com.eCommerce.common.exception.CustomException;
import com.eCommerce.common.payload.ResponseCode;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LogManager.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    private static final String DEFAULT_ROLE_CODE = "CUSTOMER";

    @Override
    public RegistrationResponse register(RegistrationRequest request) {
        try {
            // 1. Validate input (username/email tồn tại?)
            if (userRepository.existsUserByUsername(request.getUsername())) {
                throw new CustomException(ResponseCode.USERNAME_ALREADY_EXISTS);
            }

            if (userRepository.existsUserByEmail(request.getEmail())) {
                throw new CustomException(ResponseCode.EMAIL_ALREADY_EXISTS);
            }

            // 2. Lấy role mặc định cho user mua sắm (customer)
            Role defaultRole = roleRepository.findByCode(DEFAULT_ROLE_CODE)
                    .orElseThrow(() -> new CustomException(ResponseCode.ROLE_NOT_FOUND));

            // 3. Tạo entity User
            User user = User.builder()
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .isAccountNonExpired(true)
                    .isAccountNonLocked(true)
                    .isEnabled(true)
                    .roles(Set.of(defaultRole))
                    .build();

            // 4. Lưu DB
            User savedUser = userRepository.save(user);

            // 5. Generate JWT chuẩn RBAC (userId + email + roles[])
            String accessToken = jwtUtils.generateToken(savedUser);

            // 6. Lấy danh sách role code (VD: ["CUSTOMER"])
            List<String> roleCodes = savedUser.getRoles()
                    .stream()
                    .map(Role::getCode)
                    .toList();

            // 7. Build RegistrationResponse trả về (auto login)
            return RegistrationResponse.builder()
                    .userId(savedUser.getId())
                    .username(savedUser.getUsername())
                    .email(savedUser.getEmail())
                    .roles(roleCodes)
                    .accessToken(accessToken)
                    .build();

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error while registering user: {}", e.getMessage(), e);
            throw new CustomException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }
}
