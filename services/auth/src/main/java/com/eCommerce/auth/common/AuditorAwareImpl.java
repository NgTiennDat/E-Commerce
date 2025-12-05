package com.eCommerce.auth.common;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

/**
 * Implementation of AuditorAware to provide the current auditor (username)
 * for JPA auditing (created_by, updated_by fields)
 * Note: This will only be called after authentication is verified by CustomAuthorizationManager,
 * so authentication should always be present.
 */
@Component
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Nếu chưa login hoặc là anonymous → ghi SYSTEM
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            return Optional.of("SYSTEM");
        }

        Object principal = authentication.getPrincipal();

        // Nếu principal là UserDetails → lấy username
        if (principal instanceof UserDetails userDetails) {
            return Optional.ofNullable(userDetails.getUsername());
        }

        // Nếu principal là String (ví dụ JWT custom)
        if (principal instanceof String username) {
            return Optional.of(username);
        }

        // Cuối cùng fallback về authentication name
        return Optional.ofNullable(authentication.getName());
    }
}
