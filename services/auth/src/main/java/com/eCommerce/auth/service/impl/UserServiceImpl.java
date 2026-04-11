package com.eCommerce.auth.service.impl;

import com.eCommerce.auth.model.entity.Role;
import com.eCommerce.auth.model.entity.User;
import com.eCommerce.auth.model.entity.UserInfo;
import com.eCommerce.auth.model.request.AdminCreateUserRequest;
import com.eCommerce.auth.model.request.RegistrationRequest;
import com.eCommerce.auth.model.response.ProfileResponse;
import com.eCommerce.auth.model.response.RegistrationResponse;
import com.eCommerce.auth.repository.RoleRepository;
import com.eCommerce.auth.repository.UserInfoRepository;
import com.eCommerce.auth.repository.UserRepository;
import com.eCommerce.auth.service.UserService;
import com.eCommerce.auth.service.mapper.ProfileMapper;
import com.eCommerce.common.exception.CustomException;
import com.eCommerce.common.payload.ResponseCode;
import com.eCommerce.common.security.JwtUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String DEFAULT_ROLE_CODE = "CUSTOMER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final UserInfoRepository userInfoRepository;

    /**
     * Public self-registration.
     * Role luôn là CUSTOMER — server quyết định, không phải client.
     */
    @Transactional
    @Override
    public RegistrationResponse register(RegistrationRequest request) {
        try {
            validateUsernameAndEmail(request.getUsername(), request.getEmail());

            Role role = roleRepository.findByCode(DEFAULT_ROLE_CODE)
                    .orElseThrow(() -> new CustomException(ResponseCode.ROLE_NOT_FOUND));

            return buildAndSaveUser(
                    request.getUsername(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getFirstName(),
                    request.getLastName(),
                    request.getPhoneNumber(),
                    request.getAddress(),
                    request.getAvatarUrl(),
                    role
            );
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error while registering user: {}", e.getMessage(), e);
            throw new CustomException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Admin tạo user với role tùy chọn.
     * Chỉ được gọi từ AdminUserController — endpoint đó được bảo vệ bằng RBAC.
     */
    @Transactional
    @Override
    public RegistrationResponse createUser(AdminCreateUserRequest request) {
        try {
            validateUsernameAndEmail(request.getUsername(), request.getEmail());

            // Admin chỉ định role — server vẫn validate role tồn tại
            Role role = roleRepository.findByCode(request.getRoleCode())
                    .orElseThrow(() -> new CustomException(ResponseCode.ROLE_NOT_FOUND));

            return buildAndSaveUser(
                    request.getUsername(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getFirstName(),
                    request.getLastName(),
                    request.getPhoneNumber(),
                    request.getAddress(),
                    request.getAvatarUrl(),
                    role
            );
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error while creating user (admin): {}", e.getMessage(), e);
            throw new CustomException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ProfileResponse getProfile(Authentication authentication) {
        try {
            User userDetails = (User) authentication.getPrincipal();

            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new CustomException(ResponseCode.USER_NOT_FOUND));

            UserInfo userInfo = userInfoRepository.findByUser(user)
                    .orElseThrow(() -> new CustomException(ResponseCode.USER_NOT_FOUND));

            return ProfileMapper.toProfileResponse(user, userInfo);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error while fetching profile: {}", e.getMessage(), e);
            throw new CustomException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    /**
     * Validate username và email chưa tồn tại.
     * Tách ra để cả register() và createUser() dùng chung — tránh duplicate logic.
     */
    private void validateUsernameAndEmail(String username, String email) {
        if (userRepository.existsUserByUsername(username)) {
            throw new CustomException(ResponseCode.USERNAME_ALREADY_EXISTS);
        }
        if (userRepository.existsUserByEmail(email)) {
            throw new CustomException(ResponseCode.EMAIL_ALREADY_EXISTS);
        }
    }

    /**
     * Tạo User + UserInfo + generate access token, trả về RegistrationResponse.
     *
     * Tại sao extract ra private method?
     * register() và createUser() có cùng flow sau khi resolve role:
     * tạo entity → save → generate token → build response.
     * Nếu để riêng trong từng method → duplicate ~30 dòng code.
     * Extract ra đây → 1 chỗ duy nhất để sửa khi cần thay đổi.
     */
    private RegistrationResponse buildAndSaveUser(
            String username, String email, String password,
            String firstName, String lastName,
            String phoneNumber, String address, String avatarUrl,
            Role role
    ) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setIsAccountNonExpired(true);
        user.setIsAccountNonLocked(true);
        user.setIsEnabled(true);
        user.setRoles(Set.of(role));
        User savedUser = userRepository.save(user);

        UserInfo userInfo = new UserInfo();
        userInfo.setUser(savedUser);
        userInfo.setFirstName(firstName);
        userInfo.setLastName(lastName);
        userInfo.setPhoneNumber(phoneNumber);
        userInfo.setAddress(address);
        userInfo.setAvatarUrl(avatarUrl);
        userInfoRepository.save(userInfo);

        String accessToken = jwtUtils.generateAccessToken(savedUser);

        List<String> roles = savedUser.getRoles().stream()
                .map(Role::getCode)
                .toList();

        return RegistrationResponse.builder()
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .roles(roles)
                .accessToken(accessToken)
                .build();
    }
}
