package com.eCommerce.auth.handler;

import com.eCommerce.auth.entity.User;
import com.eCommerce.auth.handler.impl.UserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class CustomAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final UserDetailsServiceImpl userDetailsService;

    /**
     * Checks whether the current user is authorized to access a specific resource.
     *
     * @param authenticationSupplier A supplier that provides the current Authentication object.
     * @param requestContext         The context of the HTTP request being authorized.
     * @return An AuthorizationDecision indicating whether access is granted or denied.
     */
    @Override
    public AuthorizationDecision check(Supplier<Authentication> authenticationSupplier,
                                       RequestAuthorizationContext requestContext) {
        Authentication auth = authenticationSupplier.get();
        if (isInvalidAuthentication(auth)) {
            return new AuthorizationDecision(false);
        }

        HttpServletRequest request = requestContext.getRequest();
        String username = ((User) auth.getPrincipal()).getUsername();
        return userDetailsService.hasPermission(username, request.getServletPath(), request.getMethod())
                ? new AuthorizationDecision(true)
                : new AuthorizationDecision(false);
    }

    /**
     * Determines if the provided Authentication object is invalid.
     *
     * @param auth The Authentication object to validate.
     * @return True if the authentication is invalid, false otherwise.
     */
    private boolean isInvalidAuthentication(Authentication auth) {
        return auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken;
    }
}
