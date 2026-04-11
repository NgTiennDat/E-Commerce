package com.eCommerce.product.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Cung cấp username hiện tại cho Spring Data JPA Auditing.
 * Được dùng bởi @CreatedBy và @LastModifiedBy trong Audit base class.
 *
 * Product-service nhận request từ Gateway — JWT đã được validate tại Gateway.
 * Gateway forward username qua SecurityContext (nếu có filter extract header)
 * hoặc không có context → fallback về "SYSTEM".
 *
 * "SYSTEM" là giá trị hợp lý cho product-service vì:
 *   - Các thao tác admin (create/update product) đến từ Gateway
 *   - Gateway đã authenticate user, product-service tin tưởng Gateway
 *   - Nếu cần audit chi tiết hơn, cần thêm filter extract X-User-Id header
 */
@Component("productAuditorAware")
public class AuditorAwareImpl implements AuditorAware<String> {

    private static final String SYSTEM = "SYSTEM";

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.of(SYSTEM);
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            return Optional.ofNullable(userDetails.getUsername());
        }

        if (principal instanceof String username) {
            return Optional.of(username);
        }

        return Optional.ofNullable(authentication.getName());
    }
}
