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
 *
 * Note: This will only be called after authentication is verified by CustomAuthorizationManager,
 * so authentication should always be present.
 */
@Component
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.of("SYSTEM");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            return Optional.of(((UserDetails) principal).getUsername());
        }

        return Optional.of(authentication.getName());
    }
}
