package com.datien.customer.service;

import com.datien.customer.model.dto.CustomerResponse;
import com.datien.customer.model.dto.UpdateCustomerRequest;

import java.util.List;

public interface CustomerService {
    List<CustomerResponse> findAllCustomers(String firstName, String lastName);
    CustomerResponse findCustomer(String customerId);
    String updateCustomer(String customerId, UpdateCustomerRequest request);
    String deleteCustomer(String customerId);
}
