package com.eCommerce.auth.filter;

import com.eCommerce.auth.common.AuthConstants;
import com.eCommerce.common.security.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import io.micrometer.common.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    // AUTHORIZATION_HEADER và BEARER_PREFIX không cần khai báo lại ở đây:
    // - AUTHORIZATION_HEADER → dùng HttpHeaders.AUTHORIZATION (Spring built-in)
    // - BEARER_PREFIX        → dùng AuthConstants.BEARER_PREFIX
    // - BLACKLIST_PREFIX     → dùng AuthConstants.TOKEN_BLACKLIST

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(AuthConstants.BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(AuthConstants.BEARER_PREFIX.length());

        if (stringRedisTemplate.hasKey(AuthConstants.TOKEN_BLACKLIST + jwt)) {
            sendUnauthorizedResponse(response, "Token is blacklisted");
            return;
        }

        String username;
        try {
            username = jwtUtils.extractUsername(jwt);
        } catch (ExpiredJwtException e) {
            sendUnauthorizedResponse(response, "Token has expired");
            return;
        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            sendUnauthorizedResponse(response, "Invalid or tampered token");
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (jwtUtils.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("code", "401");
        responseBody.put("message", message);

        String json = OBJECT_MAPPER.writeValueAsString(responseBody);
        response.getWriter().write(json);
    }
}
