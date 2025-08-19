package com.datien.customer.repository;

import com.datien.customer.model.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface CustomerRepository extends MongoRepository <Customer, String> {

    @Query("{'firstName': ?0, 'lastName': ?1 }")
    Customer findCustomerByIdAndFirstNameAndLastName(String firstName, String lastName);
}
