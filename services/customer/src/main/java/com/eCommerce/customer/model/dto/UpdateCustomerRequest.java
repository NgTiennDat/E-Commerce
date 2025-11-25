package com.eCommerce.customer.model.dto;

import com.eCommerce.customer.model.Address;

public record UpdateCustomerRequest(
    String firstName,
    String lastName,
    String email,
    Address address
) {
}
