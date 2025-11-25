package com.eCommerce.common.payload;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ResponseCode {
    // ======= System Errors =======
    SYSTEM("ERR_501", "System error. Please try again later!", HttpStatus.INTERNAL_SERVER_ERROR),
    INTERNAL_SERVER_ERROR("ERR_500", "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    NO_CODE("ERR_000", "No error code specified", HttpStatus.INTERNAL_SERVER_ERROR),
    CACHE_FAILED("VAL_500", "Cache failed" , HttpStatus.INTERNAL_SERVER_ERROR),

    // ======= User ========
    USERNAME_ALREADY_EXISTS("USER_409", "Username already exists" , HttpStatus.CONFLICT),

    // ======= Email ========
    EMAIL_ALREADY_EXISTS("EMAIL_409", "Email already exists" , HttpStatus.CONFLICT),
    // ======= Role ========
    ROLE_NOT_FOUND("ROLE_404", "Role not found" , HttpStatus.NOT_FOUND),;
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ResponseCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
