package com.datien.customer.service;

import com.datien.customer.model.Customer;
import com.datien.customer.model.dto.CustomerRequest;
import com.datien.customer.model.dto.CustomerResponse;
import com.datien.customer.model.dto.UpdateCustomerRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerMapper {
    public Customer toCustomer(CustomerRequest request) {
        if(request == null) return null;
        return Customer.builder()
                .id(request.id())
                .firstName(request.firstname())
                .lastName(request.lastname())
                .email(request.email())
                .address(request.address())
                .build();
    }

    public Customer toUpdateCustomer(UpdateCustomerRequest request) {
        if(request == null) return null;
        return Customer.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .address(request.address())
                .build();
    }

    public CustomerResponse fromCustomer(Customer customer) {
        if(customer == null) return null;
        return new CustomerResponse(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getAddress()
        );
    }
}
