package com.eCommerce.auth.security;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

/**
 * Custom OAuth2 user service — xử lý user info sau khi OAuth2 provider
 * (Google, GitHub...) trả về thông tin user.
 *
 * Hiện tại chưa implement — delegate thẳng về DefaultOAuth2UserService.
 *
 * TODO: Implement khi OAuth2 login được activate:
 *   1. Nhận OAuth2User từ provider
 *   2. Tìm user trong DB theo email
 *   3. Nếu chưa có → tạo user mới với role CUSTOMER
 *   4. Nếu đã có → update thông tin nếu cần
 *   5. Trả về CustomOAuth2User implement UserDetails + OAuth2User
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // Delegate về default behavior cho đến khi implement
        return super.loadUser(userRequest);
    }
}
