package com.datien.customer.controller;

import com.datien.customer.model.dto.CustomerRequest;
import com.datien.customer.model.dto.CustomerResponse;
import com.datien.customer.model.dto.UpdateCustomerRequest;
import com.datien.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customer")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;

    @PostMapping("/add-customer")
    public ResponseEntity<String> addCustomer(
            @RequestBody CustomerRequest request
    ) {
        return ResponseEntity.ok(customerService.addCustomer(request));
    }

    @GetMapping("")
    public ResponseEntity<List<CustomerResponse>> findAllCustomers(
            @RequestParam(value = "firstName", required = false) String firstName,
            @RequestParam(value = "lastName", required = false) String lastName
    ) {
        return ResponseEntity.ok(customerService.findAllCustomers(firstName, lastName));
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> findCustomer(
            @PathVariable("customerId") String customerId
    ) {
        return ResponseEntity.ok(customerService.findCustomer(customerId));
    }

    @PutMapping("/{customerId}")
    public ResponseEntity<String> updateCustomer(
            @PathVariable("customerId") String customerId,
            @RequestBody UpdateCustomerRequest request
    ) {
        return ResponseEntity.ok(customerService.updateCustomer(customerId, request));
    }

    @DeleteMapping("/{customerId}")
    public ResponseEntity<String> deleteCustomer(
            @PathVariable("customerId") String customerId
    ) {
        return ResponseEntity.ok(customerService.deleteCustomer(customerId));
    }
}
