package com.food.ordering.system.customer.service.dataaccess.customer.mapper;

import com.food.ordering.system.customer.service.dataaccess.customer.entity.CustomerEntity;
import com.food.ordering.system.customer.service.dataaccess.customer.entity.CustomerOutboxEntity;
import com.food.ordering.system.customer.service.domain.entity.Customer;
import com.food.ordering.system.customer.service.domain.outbox.model.CustomerOutboxMessage;
import com.food.ordering.system.domain.valueobject.CustomerId;
import org.springframework.stereotype.Component;

@Component
public class CustomerDataAccessMapper {

    public Customer customerEntityToCustomer(CustomerEntity customerEntity) {
        return Customer.builder()
                .customerId(new CustomerId(customerEntity.getId()))
                .firstName(customerEntity.getFirstName())
                .username(customerEntity.getUsername())
                .customerStatus(customerEntity.getCustomerStatus())
                .lastName(customerEntity.getLastName())
                .createdAt(customerEntity.getCreatedAt())
                .build();
    }

    public CustomerEntity customerToCustomerEntity(Customer customer) {
        return CustomerEntity.builder()
                .id(customer.getId().getValue())
                .username(customer.getUsername())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .customerStatus(customer.getCustomerStatus())
                .createdAt(customer.getCreatedAt())
                .build();
    }

    public CustomerOutboxEntity orderOutboxMessageToCustomerOutboxEntity(CustomerOutboxMessage customerOutboxMessage) {
        return CustomerOutboxEntity.builder()
                .id(customerOutboxMessage.getId())
                .type(customerOutboxMessage.getType())
                .sagaId(customerOutboxMessage.getSagaId())
                .createdAt(customerOutboxMessage.getCreatedAt())
                .processedAt(customerOutboxMessage.getProcessedAt())
                .customerStatus(customerOutboxMessage.getCustomerStatus())
                .payload(customerOutboxMessage.getPayload())
                .build();
    }

    public CustomerOutboxMessage customerOutboxEntityToOrderOutboxMessage(CustomerOutboxEntity customerOutboxEntity) {
        return CustomerOutboxMessage.builder()
                .id(customerOutboxEntity.getId())
                .type(customerOutboxEntity.getType())
                .sagaStatus(customerOutboxEntity.getSagaStatus())
                .sagaId(customerOutboxEntity.getSagaId())
                .createdAt(customerOutboxEntity.getCreatedAt())
                .payload(customerOutboxEntity.getPayload())
                .processedAt(customerOutboxEntity.getProcessedAt())
                .outboxStatus(customerOutboxEntity.getOutboxStatus())
                .build();
    }
}
