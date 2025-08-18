package com.datien.customer.exception;

import com.datien.customer.common.ResponseCode;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class CustomException extends RuntimeException {
    private final ResponseCode responseCode;
}