package com.food.ordering.system.customer.service.domain.ports.output.repository;

import com.food.ordering.system.customer.service.domain.entity.Customer;

import java.util.Optional;

public interface CustomerRepository {
    Customer save(Customer customer);
    boolean customerExistsByUsername(String username);
    Optional<Customer> findById(String id);
}
