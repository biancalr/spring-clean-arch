package com.food.ordering.system.customer.service.domain.event;

import com.food.ordering.system.customer.service.domain.entity.Customer;
import com.food.ordering.system.domain.event.DomainEvent;

import java.time.ZonedDateTime;
import java.util.List;

public abstract class CustomerEvent implements DomainEvent<Customer> {
    private final Customer customer;
    private final ZonedDateTime createdAt;
    private final List<String> failureMessages;

    protected CustomerEvent(Customer customer, ZonedDateTime createdAt, List<String> failureMessages) {
        this.customer = customer;
        this.createdAt = createdAt;
        this.failureMessages = failureMessages;
    }

    public Customer getCustomer() {
        return customer;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public List<String> getFailureMessages() {
        return failureMessages;
    }
}
