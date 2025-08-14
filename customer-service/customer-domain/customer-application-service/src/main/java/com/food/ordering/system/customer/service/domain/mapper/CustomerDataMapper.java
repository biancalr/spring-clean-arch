package com.food.ordering.system.customer.service.domain.mapper;

import com.food.ordering.system.customer.service.domain.dto.create.CreateCustomerCommand;
import com.food.ordering.system.customer.service.domain.dto.create.CreateCustomerResponse;
import com.food.ordering.system.customer.service.domain.entity.Customer;
import com.food.ordering.system.customer.service.domain.event.CustomerEvent;
import com.food.ordering.system.customer.service.domain.outbox.model.CustomerEventPayload;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

import static java.time.ZoneOffset.UTC;

@Component
public class CustomerDataMapper {

    public Customer customerRequestModelToCustomer(
            CreateCustomerCommand createCustomerCommand) {
        return Customer.builder()
                .username(createCustomerCommand.getUsername())
                .firstName(createCustomerCommand.getFirstName())
                .lastName(createCustomerCommand.getLastName())
                .build();
    }

    public CustomerEventPayload customerEventToOrderEventPayload(
            CustomerEvent customerEvent) {
        return CustomerEventPayload.builder()
                .customerId(customerEvent.getCustomer().getId().getValue().toString())
                .username(customerEvent.getCustomer().getUsername())
                .firstName(customerEvent.getCustomer().getFirstName())
                .lastName(customerEvent.getCustomer().getLastName())
                .createdAt(customerEvent.getCreatedAt())
                .customerStatus(customerEvent.getCustomer().getCustomerStatus().name())
                .build();
    }

    public CustomerEventPayload customerToOrderEventPayload(Customer customer,
                                                            Instant createdAt) {
        return CustomerEventPayload.builder()
                .customerId(customer.getId().getValue().toString())
                .customerStatus(customer.getCustomerStatus().name())
                .firstName(customer.getFirstName())
                .username(customer.getUsername())
                .lastName(customer.getLastName())
                .createdAt(createdAt.atZone(UTC))
                .build();
    }

    public CreateCustomerResponse customerToCreateCustomerResponse(Customer customer, String message) {
        return CreateCustomerResponse.builder()
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .customerStatus(customer.getCustomerStatus())
                .customerId(customer.getId().getValue())
                .username(customer.getUsername())
                .message(message)
                .build();
    }
}
