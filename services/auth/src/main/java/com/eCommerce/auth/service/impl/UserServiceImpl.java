package com.eCommerce.auth.service.impl;

import com.eCommerce.auth.model.entity.Role;
import com.eCommerce.auth.model.entity.User;
import com.eCommerce.auth.model.entity.UserInfo;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger logger = LogManager.getLogger(UserServiceImpl.class);
    private static final String SYSTEM = "SYSTEM";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    private static final String DEFAULT_ROLE_CODE = "CUSTOMER";
    private final UserInfoRepository userInfoRepository;

    /**
     * Register a new user.
     * @param request the registration request containing user details
     * @return the registration response with user info and access token
     * @throws CustomException if validation fails or an error occurs
     */
    @Transactional
    @Override
    public RegistrationResponse register(RegistrationRequest request) {
        try {

            // ========== 1. Validate ==========
            if (userRepository.existsUserByUsername(request.getUsername())) {
                throw new CustomException(ResponseCode.USERNAME_ALREADY_EXISTS);
            }

            if (userRepository.existsUserByEmail(request.getEmail())) {
                throw new CustomException(ResponseCode.EMAIL_ALREADY_EXISTS);
            }

            // ========== 2. Resolve Role ==========
            String roleCode = (request.getRoleCode() == null || request.getRoleCode().isBlank())
                    ? DEFAULT_ROLE_CODE
                    : request.getRoleCode();

            Role role = roleRepository.findByCode(roleCode)
                    .orElseThrow(() -> new CustomException(ResponseCode.ROLE_NOT_FOUND));

            // ========== 3. Create User ==========
            User user = new User();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setIsAccountNonExpired(true);
            user.setIsAccountNonLocked(true);
            user.setIsEnabled(true); // allow login
            user.setRoles(Set.of(role));

            // Save user first
            User savedUser = userRepository.save(user);

            // ========== 4. Create UserInfo ==========
            UserInfo userInfo = new UserInfo();
            userInfo.setUser(savedUser);
            userInfo.setFirstName(request.getFirstName());
            userInfo.setLastName(request.getLastName());
            userInfo.setPhoneNumber(request.getPhoneNumber());
            userInfo.setAddress(request.getAddress());
            userInfo.setAvatarUrl(request.getAvatarUrl());

            userInfoRepository.save(userInfo);

            // ========== 5. Generate JWT ==========
            String accessToken = jwtUtils.generateToken(savedUser);

            List<String> roles = savedUser.getRoles()
                    .stream()
                    .map(Role::getCode)
                    .toList();

            // ========== 6. Build Response ==========
            return RegistrationResponse.builder()
                    .userId(savedUser.getId())
                    .username(savedUser.getUsername())
                    .email(savedUser.getEmail())
                    .roles(roles)
                    .accessToken(accessToken)
                    .build();

        } catch (CustomException e) {
            throw e;

        } catch (Exception e) {
            logger.error("Error while registering user: {}", e.getMessage(), e);
            throw new CustomException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ProfileResponse getProfile(Authentication authentication) {
        try {
            var principal = authentication.getPrincipal();

            // Ép kiểu về UserDetailsImpl hoặc User entity (tuỳ bạn)
            User userDetails = (User) principal;

            // Lấy User
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new CustomException(ResponseCode.USER_NOT_FOUND));

            // Lấy UserInfo
            UserInfo userInfo = userInfoRepository.findByUser(user)
                    .orElseThrow(() -> new CustomException(ResponseCode.USER_NOT_FOUND));

            // Map sang response DTO
            return ProfileMapper.toProfileResponse(user, userInfo);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error while registering user: {}", e.getMessage(), e);
            throw new CustomException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

}
