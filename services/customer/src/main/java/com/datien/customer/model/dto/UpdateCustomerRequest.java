package com.datien.customer.model.dto;

import com.datien.customer.model.Address;

public record UpdateCustomerRequest(
    String firstName,
    String lastName,
    String email,
    Address address
) {
}
