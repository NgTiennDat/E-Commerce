package com.eCommerce.common.exception;

import com.eCommerce.common.payload.FieldViolation;
import com.eCommerce.common.payload.Response;
import com.eCommerce.common.payload.ResponseCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<Void>> handleGenericException(Exception ex) {
        return buildErrorResponse(ResponseCode.SYSTEM);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Response<Void>> handleAccessDeniedException(AccessDeniedException e) {
        return buildErrorResponse(e.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Response<Void>> handleCustomException(CustomException ex) {
        ResponseCode code = ex.getResponseCode();
        return buildErrorResponse(code);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Response<Void>> handleRuntimeException(RuntimeException e) {
        return buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Response<Void>> handleValidationException(BindException ex) {
        List<FieldViolation> violations = ex.getBindingResult().getFieldErrors().stream()
                .map(this::mapFieldErrorToViolation)
                .collect(Collectors.toList());

        Response<Void> response = new Response<>();
        Response.Metadata metadata = new Response.Metadata();
        metadata.setCode("400");
        metadata.setMessage("Validation failed");
        metadata.setErrors(violations);
        response.setMeta(metadata);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Response<Void>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ResponseCode code = ex.getResponseCode();
        return buildErrorResponse(code);
    }

    /**
     * Map FieldError to FieldViolation for validation errors.
     *
     * @param fieldError the FieldError
     * @return FieldViolation
     */
    private FieldViolation mapFieldErrorToViolation(FieldError fieldError) {
        return new FieldViolation(fieldError.getField(), fieldError.getDefaultMessage());
    }

    private ResponseEntity<Response<Void>> buildErrorResponse(String message, HttpStatus status) {
        Response<Void> response = new Response<>();
        Response.Metadata metadata = new Response.Metadata();
        metadata.setCode(String.valueOf(status.value()));
        metadata.setMessage(message);
        response.setMeta(metadata);

        return ResponseEntity.status(status).body(response);
    }

    private ResponseEntity<Response<Void>> buildErrorResponse(ResponseCode code) {
        Response<Void> response = new Response<>();
        Response.Metadata metadata = new Response.Metadata();
        metadata.setCode(code.getCode());
        metadata.setMessage(code.getMessage());
        response.setMeta(metadata);

        return ResponseEntity.status(code.getHttpStatus()).body(response);
    }


}