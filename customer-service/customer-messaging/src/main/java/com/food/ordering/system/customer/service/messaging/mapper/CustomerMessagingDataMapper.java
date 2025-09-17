package com.food.ordering.system.customer.service.messaging.mapper;
import com.food.ordering.system.customer.service.domain.event.CustomerCreatedEvent;
import com.food.ordering.system.kafka.order.avro.model.CustomerRequestAvroModel;
import com.food.ordering.system.kafka.order.avro.model.CustomerStatus;
import org.springframework.stereotype.Component;

@Component
public class CustomerMessagingDataMapper {

    public CustomerRequestAvroModel
    customerResponseAvroModelToCustomerResponse(
            CustomerCreatedEvent customerCreatedEvent) {
        return CustomerRequestAvroModel.newBuilder()
                .setId(customerCreatedEvent.getCustomer().getId().getValue().toString())
                .setCreatedAt(customerCreatedEvent.getCreatedAt().toInstant())
                .setFirstName(customerCreatedEvent.getCustomer().getFirstName())
                .setLastName(customerCreatedEvent.getCustomer().getLastName())
                .setUsername(customerCreatedEvent.getCustomer().getUsername())
                .build();
    }
}
