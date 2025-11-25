package com.eCommerce.customer.service.impl;

import com.eCommerce.common.exception.CustomException;
import com.eCommerce.common.payload.ResponseCode;
import com.eCommerce.customer.model.Customer;
import com.eCommerce.customer.model.dto.CustomerRequest;
import com.eCommerce.customer.model.dto.CustomerResponse;
import com.eCommerce.customer.model.dto.UpdateCustomerRequest;
import com.eCommerce.customer.repository.CustomerRepository;
import com.eCommerce.customer.service.CustomerMapper;
import com.eCommerce.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerMapper customerMapper;
    private final CustomerRepository customerRepository;
    private final MongoTemplate mongoTemplate;

    public String addCustomer(CustomerRequest request) {
        var customer = customerMapper.toCustomer(request);
        customerRepository.save(customer);
        return "Customer added with id: " + customer.getId();
    }

    public List<CustomerResponse> findAllCustomers(String firstName, String lastName) {
        Query query = new Query();

        if (firstName != null && !firstName.isBlank()) {
            query.addCriteria(Criteria.where("firstName").is(firstName));
        }

        if (lastName != null && !lastName.isBlank()) {
            query.addCriteria(Criteria.where("lastName").is(lastName));
        }

        List<Customer> customers = mongoTemplate.find(query, Customer.class);

        return customers.stream()
                .map(customerMapper::fromCustomer)
                .collect(Collectors.toList());
    }

    public CustomerResponse findCustomer(String customerId) {
        var customers = customerRepository.findById(customerId);
        if(customers.isEmpty()) {
            throw new CustomException(ResponseCode.NO_CODE);
        }
        return customerMapper.fromCustomer(customers.get());
    }

    public String updateCustomer(String customerId, UpdateCustomerRequest request) {
        var customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));
        var updateCustomer = customerMapper.toUpdateCustomer(request);
        customerRepository.save(updateCustomer);
        return "Customer updated with id: " + customer.getId();
    }

    public String deleteCustomer(String customerId) {
        var customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));
        customerRepository.delete(customer);
        return "Customer deleted with id: " + customer.getId();
    }
}
