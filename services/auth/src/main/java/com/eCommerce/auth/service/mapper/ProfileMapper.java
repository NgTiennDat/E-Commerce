package com.eCommerce.auth.service.mapper;

import com.eCommerce.auth.model.entity.Role;
import com.eCommerce.auth.model.entity.User;
import com.eCommerce.auth.model.entity.UserInfo;
import com.eCommerce.auth.model.response.ProfileResponse;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class ProfileMapper {

    public static ProfileResponse toProfileResponse(User user, UserInfo userInfo) {
        if (user == null) {
            return null;
        }

        List<String> roleCodes = user.getRoles() == null
                ? Collections.emptyList()
                : user.getRoles()
                .stream()
                .filter(Objects::nonNull)
                .map(Role::getCode)
                .toList();

        String firstName = userInfo != null ? userInfo.getFirstName() : null;
        String lastName = userInfo != null ? userInfo.getLastName() : null;
        String fullName = userInfo != null ? userInfo.getFullName() : null;

        return ProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(firstName)
                .lastName(lastName)
                .fullName(fullName)
                .phoneNumber(userInfo != null ? userInfo.getPhoneNumber() : null)
                .address(userInfo != null ? userInfo.getAddress() : null)
                .avatarUrl(userInfo != null ? userInfo.getAvatarUrl() : null)

                .accountNonLocked(user.getIsAccountNonLocked())
                .accountNonExpired(user.getIsAccountNonExpired())
                .enabled(user.getIsEnabled())

                .roles(roleCodes)

                .createdBy(user.getCreatedBy())
                .createdAt(user.getCreatedAt())
                .updatedBy(user.getUpdatedBy())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
