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
    INVALID_REQUEST("VAL_400", "Invalid request" , HttpStatus.BAD_REQUEST),

    // ======= User ========
    USERNAME_ALREADY_EXISTS("USER_409", "Username already exists" , HttpStatus.CONFLICT),
    USER_NOT_FOUND("USER_404", "User not found", HttpStatus.NOT_FOUND),
    INVALID_CREDENTIALS("USER_403", "Invalid credentials", HttpStatus.UNAUTHORIZED),
    // ======= Email ========
    EMAIL_ALREADY_EXISTS("EMAIL_409", "Email already exists" , HttpStatus.CONFLICT),
    // ======= Role ========
    ROLE_NOT_FOUND("ROLE_404", "Role not found" , HttpStatus.NOT_FOUND),

    // ======= Product ========
    PRODUCT_NOT_FOUND("PRD_404", "Product not found", HttpStatus.NOT_FOUND),
    PRODUCT_QUANTITY_NOT_ENOUGH("PRD_400", "Product quantity not enough", HttpStatus.BAD_REQUEST),

    // ======= Category ========
    CATEGORY_NOT_FOUND("CAT_404", "Category not found" , HttpStatus.NOT_FOUND),

    // ======= Customer ========
    CUSTOMER_NOT_FOUND("CUS_404", "Customer not found", HttpStatus.NOT_FOUND),

    // ======= Order ========
    ORDER_NOT_FOUND("ORD_404", "Order not found", HttpStatus.NOT_FOUND),
    ORDER_CANNOT_BE_CANCELLED("ORD_400", "Order cannot be cancelled in current status", HttpStatus.BAD_REQUEST),

    // ======= File ========
    FILE_EMPTY("FILE_400", "File must not be empty", HttpStatus.BAD_REQUEST),
    FILE_TOO_LARGE("FILE_413", "File size exceeds the maximum allowed limit", HttpStatus.PAYLOAD_TOO_LARGE),
    FILE_TYPE_NOT_SUPPORTED("FILE_415", "File type is not supported", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    FILE_UPLOAD_FAILED("FILE_500", "Failed to upload file", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_DELETE_FAILED("FILE_501", "Failed to delete file", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_NOT_FOUND("FILE_404", "File not found", HttpStatus.NOT_FOUND),
    STORAGE_UNAVAILABLE("FILE_503", "Storage service is unavailable", HttpStatus.SERVICE_UNAVAILABLE);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ResponseCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
