package com.food.ordering.system.customer.service.domain.outbox.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.ordering.system.customer.service.domain.exception.CustomerDomainException;
import com.food.ordering.system.customer.service.domain.outbox.model.CustomerEventPayload;
import com.food.ordering.system.customer.service.domain.outbox.model.CustomerOutboxMessage;
import com.food.ordering.system.customer.service.domain.ports.output.repository.CustomerOutboxRepository;
import com.food.ordering.system.domain.valueobject.CustomerStatus;
import com.food.ordering.system.outbox.OutboxStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.food.ordering.system.domain.constant.DomainConstants.UTC;
import static com.food.ordering.system.saga.order.SagaConstants.CUSTOMER_SAGA_NAME;

@Slf4j
@Component
public class CustomerOutboxHelper {

    private final CustomerOutboxRepository customerOutboxRepository;
    private final ObjectMapper objectMapper;

    public CustomerOutboxHelper(CustomerOutboxRepository customerOutboxRepository, ObjectMapper objectMapper) {
        this.customerOutboxRepository = customerOutboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public Optional<CustomerOutboxMessage> getCompletedOrderOutboxMessageBySagaIdAndCustomerStatus(
            UUID sagaId,
            OutboxStatus outboxStatus) {
        return customerOutboxRepository
                .findByTypeAndSagaIdAndOutboxStatus(CUSTOMER_SAGA_NAME, sagaId, outboxStatus);
    }

    @Transactional
    public void saveOrderOutboxMessage(
            CustomerEventPayload customerEventPayload,
            CustomerStatus customerStatus,
            OutboxStatus outboxStatus,
            UUID sagaId) {
        save(CustomerOutboxMessage.builder()
                .id(UUID.randomUUID())
                .sagaId(sagaId)
                .createdAt(customerEventPayload.getCreatedAt())
                .processedAt(ZonedDateTime.now(ZoneId.of(UTC)))
                .type(CUSTOMER_SAGA_NAME)
                .payload(createPayload(customerEventPayload))
                .customerStatus(customerStatus)
                .outboxStatus(outboxStatus)
                .build());
    }

    @Transactional(readOnly = true)
    public Optional<List<CustomerOutboxMessage>> getOrderOutboxMessageByOutboxStatus(OutboxStatus outboxStatus) {
        return customerOutboxRepository.findByTypeAndOutboxStatus(CUSTOMER_SAGA_NAME, outboxStatus);
    }

    @Transactional
    public void deleteOrderOutboxMessageByOutboxStatus(OutboxStatus outboxStatus) {
        customerOutboxRepository.deleteByTypeAndOutboxStatus(CUSTOMER_SAGA_NAME, outboxStatus);
    }

    @Transactional
    public void updateOutboxStatus(
            CustomerOutboxMessage customerOutboxMessage,
            OutboxStatus outboxStatus) {
        customerOutboxMessage.setOutboxStatus(outboxStatus);
        save(customerOutboxMessage);
        log.info("Order outbox table status is updated as: {}", outboxStatus.name());
    }

    private void save(CustomerOutboxMessage customerOutboxMessage) {
        CustomerOutboxMessage response = customerOutboxRepository.save(customerOutboxMessage);
        if (response == null) {
            throw new CustomerDomainException("Could not save CustomerOutboxMessage!");
        }
        log.info("CustomerOutboxMessage saved with id: {}", customerOutboxMessage.getId());
    }

    private String createPayload(CustomerEventPayload customerEventPayload) {
        try {
            return objectMapper.writeValueAsString(customerEventPayload);
        } catch (JsonProcessingException e) {
            log.error("Could not create CustomerEventPayload json!", e);
            throw new CustomerDomainException("Could not create CustomerEventPayload json!", e);
        }
    }
}
