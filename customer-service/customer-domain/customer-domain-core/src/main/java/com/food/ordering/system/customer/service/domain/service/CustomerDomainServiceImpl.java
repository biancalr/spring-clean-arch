package com.food.ordering.system.customer.service.domain.service;

import com.food.ordering.system.customer.service.domain.entity.Customer;
import com.food.ordering.system.customer.service.domain.event.CustomerCreatedEvent;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static com.food.ordering.system.domain.constant.DomainConstants.UTC;

@Slf4j
public class CustomerDomainServiceImpl implements CustomerDomainService {

    @Override
    public CustomerCreatedEvent validateAndInitiateCustomer(final Customer customer) {
        customer.validateCustomer(customer);
        customer.initializeCustomer();
        log.info("Customer processing running for new user with id {}", customer.getId().getValue().toString());
        return new CustomerCreatedEvent(customer, ZonedDateTime.now(ZoneId.of(UTC)));
    }
}
