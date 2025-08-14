package com.food.ordering.system.customer.service.domain.event;

import com.food.ordering.system.customer.service.domain.entity.Customer;

import java.time.ZonedDateTime;
import java.util.List;

public class CustomerFailedEvent extends CustomerEvent {

    public CustomerFailedEvent(Customer customer, ZonedDateTime createdAt, List<String> failureMessages) {
        super(customer, createdAt, failureMessages);
    }
}
