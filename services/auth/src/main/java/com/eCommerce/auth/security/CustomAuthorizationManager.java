package com.eCommerce.auth.security;

import com.eCommerce.auth.model.entity.User;
import com.eCommerce.auth.service.PermissionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * RBAC authorization manager cho auth-service internal endpoints.
 * Dùng PermissionService để check quyền dựa trên username + path + method.
 */
@Component
@RequiredArgsConstructor
public class CustomAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final PermissionService permissionService;

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authenticationSupplier,
                                       RequestAuthorizationContext requestContext) {
        Authentication auth = authenticationSupplier.get();
        if (isInvalidAuthentication(auth)) {
            return new AuthorizationDecision(false);
        }

        HttpServletRequest request = requestContext.getRequest();
        Object principal = auth.getPrincipal();
        if (!(principal instanceof User user)) {
            return new AuthorizationDecision(false);
        }

        boolean allowed = permissionService.hasPermission(
                user.getUsername(), request.getServletPath(), request.getMethod()
        );
        return new AuthorizationDecision(allowed);
    }

    private boolean isInvalidAuthentication(Authentication auth) {
        return auth == null
                || !auth.isAuthenticated()
                || auth instanceof AnonymousAuthenticationToken;
    }
}
