package com.eCommerce.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@ConditionalOnProperty(
        name = "app.jwt.enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class JwtUtils {

    // Claim key dùng để phân biệt loại token
    // Tại sao cần? Để ngăn access token bị dùng như refresh token và ngược lại.
    public static final String CLAIM_TOKEN_TYPE  = "type";
    public static final String TOKEN_TYPE_ACCESS  = "access";
    public static final String TOKEN_TYPE_REFRESH = "refresh";

    @Value("${application.security.jwt.expiration}")
    private long accessExpirationMillis;

    // Đọc refresh expiry riêng — trước đây field này không tồn tại trong JwtUtils
    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpirationMillis;

    @Value("${application.security.jwt.secret-key}")
    private String secret;

    // ====================== API CHUNG ======================

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return extractClaim(token, claims -> {
            Object roles = claims.get("roles");
            if (roles instanceof List<?> list) {
                return list.stream().map(Object::toString).collect(Collectors.toList());
            }
            return Collections.emptyList();
        });
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        final Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    // =================== GENERATE TOKEN ====================

    /**
     * Tạo ACCESS token.
     * - Short-lived (accessExpirationMillis, mặc định 15 phút)
     * - Chứa roles để downstream services authorize mà không cần gọi DB
     * - Embed claim type=access để server phân biệt với refresh token
     */
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        claims.put("roles", roles);
        claims.put(CLAIM_TOKEN_TYPE, TOKEN_TYPE_ACCESS);
        return buildToken(claims, userDetails.getUsername(), accessExpirationMillis);
    }

    /**
     * Tạo REFRESH token.
     * - Long-lived (refreshExpirationMillis, mặc định 7 ngày)
     * - Chỉ chứa subject (username) và type=refresh, KHÔNG chứa roles
     *   Lý do: refresh token không dùng để authorize request, chỉ dùng để lấy access token mới.
     *   Giữ payload nhỏ và tách biệt mục đích.
     * - Embed claim type=refresh để ngăn dùng nhầm như access token
     */
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_TOKEN_TYPE, TOKEN_TYPE_REFRESH);
        return buildToken(claims, userDetails.getUsername(), refreshExpirationMillis);
    }

    /**
     * Giữ lại để không break các caller cũ trong lúc migration.
     * @deprecated Dùng generateAccessToken() hoặc generateRefreshToken() thay thế.
     */
    @Deprecated(forRemoval = true)
    public String generateToken(UserDetails userDetails) {
        return generateAccessToken(userDetails);
    }

    /**
     * @deprecated Dùng generateAccessToken() thay thế.
     */
    @Deprecated(forRemoval = true)
    public String generateToken(String username, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        claims.put(CLAIM_TOKEN_TYPE, TOKEN_TYPE_ACCESS);
        return buildToken(claims, username, accessExpirationMillis);
    }

    private String buildToken(Map<String, Object> extraClaims, String subject, long expirationMillis) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ======================= VALIDATE ======================

    /**
     * Validate access token trong auth-filter:
     * - Đúng username
     * - Chưa hết hạn
     * - Đúng type=access (ngăn refresh token bị dùng để authenticate request)
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername())
                && isNotExpired(token)
                && isAccessToken(token);
    }

    /**
     * Validate token không cần UserDetails — dùng cho Gateway.
     * Chỉ check signature + expiry, không check type.
     * Gateway không cần biết type, chỉ cần biết token hợp lệ.
     */
    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return isNotExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Kiểm tra token có phải access token không.
     * Dùng tại /auth/refresh để từ chối nếu client gửi nhầm access token.
     */
    public boolean isAccessToken(String token) {
        String type = extractClaim(token, claims -> claims.get(CLAIM_TOKEN_TYPE, String.class));
        return TOKEN_TYPE_ACCESS.equals(type);
    }

    /**
     * Kiểm tra token có phải refresh token không.
     * Dùng tại /auth/refresh để đảm bảo đúng loại token được gửi lên.
     */
    public boolean isRefreshToken(String token) {
        String type = extractClaim(token, claims -> claims.get(CLAIM_TOKEN_TYPE, String.class));
        return TOKEN_TYPE_REFRESH.equals(type);
    }

    // ==================== INTERNAL =========================

    /**
     * Trả về true nếu token CHƯA hết hạn.
     * Tên cũ isTokenExpired() gây nhầm lẫn vì trả về true khi còn hạn.
     * Đổi tên thành isNotExpired() để rõ nghĩa hơn.
     */
    private boolean isNotExpired(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        return !expiration.before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
