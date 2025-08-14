package com.food.ordering.system.customer.service.messaging.mapper;

import com.food.ordering.system.customer.service.domain.dto.message.CustomerResponse;
import com.food.ordering.system.customer.service.domain.outbox.model.CustomerEventPayload;
import com.food.ordering.system.domain.valueobject.CustomerStatus;
import com.food.ordering.system.kafka.order.avro.model.CustomerRequestAvroModel;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CustomerMessagingDataMapper {

    public CustomerResponse
    customerRequestAvroModelResponseAvroModeloCustomerResponse(
            CustomerRequestAvroModel customerAvroModel) {
        return CustomerResponse.builder()
                .id(customerAvroModel.getId())
                .createdAt(customerAvroModel.getCreatedAt())
                .customerStatus(CustomerStatus.valueOf(customerAvroModel.getCustomerStatus().name()))
                .sagaId(customerAvroModel.getSagaId())
                .build();
    }

    public CustomerRequestAvroModel
    orderEventPayloadToAvroModelToCustomerApproval(
            String sagaId,
            CustomerEventPayload customerEventPayload) {
        return CustomerRequestAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setSagaId(sagaId)
                .setCustomerStatus(com.food.ordering.system.kafka.order.avro.model.CustomerStatus.valueOf(customerEventPayload.getCustomerStatus()))
                .setCreatedAt(customerEventPayload.getCreatedAt().toInstant())
                .build();
    }
}
