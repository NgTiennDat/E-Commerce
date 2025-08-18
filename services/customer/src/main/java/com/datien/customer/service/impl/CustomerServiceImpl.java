package com.datien.customer.service.impl;

import com.datien.customer.common.ResponseCode;
import com.datien.customer.exception.CustomException;
import com.datien.customer.model.Customer;
import com.datien.customer.model.dto.CustomerRequest;
import com.datien.customer.model.dto.CustomerResponse;
import com.datien.customer.model.dto.UpdateCustomerRequest;
import com.datien.customer.CustomerRepository;
import com.datien.customer.service.CustomerMapper;
import com.datien.customer.service.CustomerService;
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
