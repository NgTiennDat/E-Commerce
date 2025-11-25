package com.eCommerce.customer.exception;

import com.eCommerce.customer.common.ResponseCode;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class CustomException extends RuntimeException {
    private final ResponseCode responseCode;
}