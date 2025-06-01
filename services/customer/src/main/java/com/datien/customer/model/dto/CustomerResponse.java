package com.datien.customer.model.dto;

import com.datien.customer.model.Address;

public record CustomerResponse(
    String id,
    String firstName,
    String lastName,
    String email,
    Address address
) {
}
