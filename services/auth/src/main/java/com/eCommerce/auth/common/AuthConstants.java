package com.eCommerce.auth.common;

/**
 * Tập trung toàn bộ string constants dùng chung trong auth-service.
 *
 * Tại sao cần file này?
 * Các string như Redis key prefix hay cookie name được dùng ở nhiều class
 * (AuthServiceImpl, JwtAuthFilter, RefreshTokenServiceImpl...).
 * Nếu mỗi class tự định nghĩa riêng, chỉ cần 1 bên typo hoặc đổi giá trị
 * mà không đồng bộ → silent bug (logout không hoạt động, token không bị blacklist...).
 * Tách ra đây → compiler enforce consistency, không cần dựa vào convention.
 *
 * Quy tắc thêm constant vào đây:
 *   - Constant được dùng ở ít nhất 2 class khác nhau, HOẶC
 *   - Constant đại diện cho một "contract" (ví dụ: Redis key schema)
 *     mà nếu thay đổi sẽ ảnh hưởng đến nhiều component.
 *
 * Quy tắc KHÔNG thêm vào đây:
 *   - Implementation detail chỉ dùng trong 1 class (ví dụ: MAX_ATTEMPTS của RateLimiter)
 *   - Config value nên đọc từ application.yml thay vì hardcode
 */
public final class AuthConstants {

    // Không cho phép khởi tạo — đây là utility class chứa constants
    private AuthConstants() {}

    // =====================================================================
    // Redis Key Prefixes
    // Quy ước: dùng ":" làm separator, lowercase, mô tả rõ namespace
    // Ví dụ key thực tế: "auth:refresh_token:42", "auth:blacklist:eyJ..."
    // =====================================================================

    /**
     * Prefix cho refresh token lưu theo token value — hỗ trợ multi-device.
     * Key đầy đủ: REFRESH_TOKEN_BY_VALUE + tokenString → username
     * Dùng ở: RefreshTokenServiceImpl (ghi khi login, đọc khi validate, xóa khi revoke)
     */
    public static final String REFRESH_TOKEN_BY_VALUE = "auth:rt:token:";

    /**
     * Prefix cho refresh token đã bị revoke (blacklist).
     * Key đầy đủ: REFRESH_TOKEN_BLACKLIST + tokenString
     * Dùng ở: RefreshTokenServiceImpl (ghi khi revoke, đọc khi validate)
     */
    public static final String REFRESH_TOKEN_BLACKLIST = "auth:rt:blacklist:";

    /**
     * Prefix cho access token đã bị blacklist sau khi logout.
     * Key đầy đủ: TOKEN_BLACKLIST + rawJwtToken
     * Dùng ở: AuthServiceImpl.logout (ghi), JwtAuthFilter (đọc)
     *
     * QUAN TRỌNG: Cả 2 nơi phải dùng CÙNG prefix này.
     * Nếu 1 bên đổi mà bên kia không biết → logout không hoạt động.
     */
    public static final String TOKEN_BLACKLIST = "auth:blacklist:";

    // =====================================================================
    // Cookie
    // =====================================================================

    /**
     * Tên cookie chứa refresh token (HttpOnly, Secure).
     * Dùng ở: AuthServiceImpl (set cookie), AuthServiceImpl (đọc cookie khi logout)
     */
    public static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    // =====================================================================
    // HTTP
    // =====================================================================

    /**
     * Prefix của Authorization header value.
     * Ví dụ header: "Authorization: Bearer eyJ..."
     * Dùng ở: JwtAuthFilter, AuthServiceImpl.logout
     *
     * Lưu ý: Spring đã có HttpHeaders.AUTHORIZATION cho tên header.
     * Constant này chỉ cho phần prefix "Bearer " (có space ở cuối — intentional).
     */
    public static final String BEARER_PREFIX = "Bearer ";

    // =====================================================================
    // Internal Service-to-Service
    // =====================================================================

    /**
     * Tên HTTP header dùng để authenticate request nội bộ giữa các service.
     *
     * Tại sao cần header riêng thay vì dùng Authorization?
     * Authorization header đã được dùng cho user JWT.
     * Dùng chung sẽ gây nhầm lẫn về loại credential đang được validate.
     * Header riêng tách biệt rõ 2 loại trust: user trust vs service trust.
     *
     * Dùng ở: RbacInternalController (đọc + validate), RbacClient (ghi khi gọi)
     */
    public static final String INTERNAL_SECRET_HEADER = "X-Internal-Secret";
}
