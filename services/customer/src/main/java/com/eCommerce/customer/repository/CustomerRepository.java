package com.eCommerce.customer.repository;

import com.eCommerce.customer.model.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface CustomerRepository extends MongoRepository<Customer, String> {

    @Query("{'firstName': ?0, 'lastName': ?1}")
    Customer findCustomerByIdAndFirstNameAndLastName(String firstName, String lastName);

    // Dùng bởi Order Service (qua Feign) để lookup customerId theo email
    Optional<Customer> findByEmail(String email);
}
