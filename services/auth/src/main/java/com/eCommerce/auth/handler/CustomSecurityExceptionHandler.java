package com.eCommerce.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomSecurityExceptionHandler implements AccessDeniedHandler, AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        writeResponse(response,
                HttpServletResponse.SC_UNAUTHORIZED,
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase());
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        writeResponse(response,
                HttpServletResponse.SC_FORBIDDEN,
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase());
    }

    private void writeResponse(HttpServletResponse response, int status,
                               int code, String titleError) throws IOException {
        Locale locale = LocaleContextHolder.getLocale();
        String message = messageSource.getMessage(titleError, null, "An unexpected error occurred", locale);
        Map<String, Object> body = new HashMap<>();

        body.put("code", code);
        body.put("message", message);
        body.put("titleError", titleError);

        writeJsonResponse(response, status, body);
    }

    private void writeJsonResponse(HttpServletResponse response, int status, Map<String, Object> body)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.addHeader("Expires", "Mon, 26 Jul 1997 05:00:00 GMT");
        response.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, proxy-revalidate");
        response.addHeader("Content-Type", "application/json; charset=utf-8");
        response.addHeader("X-Content-Type-Options", "nosniff");
        response.addHeader("X-Robots-Tag", "noindex, nofollow");
        response.addHeader("X-XSS-Protection", "1; mode=block");
        response.addHeader("X-Frame-Options", "DENY");

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}