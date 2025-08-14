package com.food.ordering.system.customer.service.domain.outbox.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.ordering.system.customer.service.domain.exception.CustomerDomainException;
import com.food.ordering.system.customer.service.domain.outbox.model.CustomerOutboxMessage;
import com.food.ordering.system.customer.service.domain.outbox.model.CustomerEventPayload;
import com.food.ordering.system.customer.service.domain.ports.output.message.publisher.CustomerResponseMessagePublisher;
import com.food.ordering.system.customer.service.domain.ports.output.repository.CustomerOutboxRepository;
import com.food.ordering.system.domain.valueobject.CustomerStatus;
import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.food.ordering.system.saga.order.SagaConstants.CUSTOMER_SAGA_NAME;

@Slf4j
@Component
public class CustomerOutboxScheduler implements OutboxScheduler {

    private final CustomerOutboxHelper customerOutboxHelper;
    private final CustomerResponseMessagePublisher customerResponseMessagePublisher;
    private final ObjectMapper objectMapper;
    private final CustomerOutboxRepository customerOutboxRepository;

    public CustomerOutboxScheduler(CustomerOutboxHelper customerOutboxHelper, CustomerResponseMessagePublisher customerResponseMessagePublisher, ObjectMapper objectMapper, CustomerOutboxRepository customerOutboxRepository) {
        this.customerOutboxHelper = customerOutboxHelper;
        this.customerResponseMessagePublisher = customerResponseMessagePublisher;
        this.objectMapper = objectMapper;
        this.customerOutboxRepository = customerOutboxRepository;
    }

    @Transactional
    @Scheduled(fixedRateString = "${customer-service.outbox-scheduler-fixed-rate}",
            initialDelayString = "${customer-service.outbox-scheduler-initial-delay}")
    @Override
    public void processOutboxMessage() {
        Optional<List<CustomerOutboxMessage>> outboxMessagesResponse =
                customerOutboxHelper.getOrderOutboxMessageByOutboxStatus(
                        OutboxStatus.STARTED);
        if (outboxMessagesResponse.isPresent() && !outboxMessagesResponse.get().isEmpty()) {
            List<CustomerOutboxMessage> outboxMessages = outboxMessagesResponse.get();
            log.info("Received {} CustomerOutboxMessage with ids {}, sending to message bus!",
                    outboxMessages.size(),
                    outboxMessages.stream().map(outboxMessage ->
                            outboxMessage.getId().toString()).collect(Collectors.joining(",")));
            outboxMessages.forEach(orderOutboxMessage ->
                    customerResponseMessagePublisher.publish(orderOutboxMessage,
                            customerOutboxHelper::updateOutboxStatus));
            log.info("{} CustomerOutboxMessage sent to message bus!", outboxMessages.size());
        }
    }

    public void customerRegisterOutboxMessage(
            CustomerEventPayload customerEventPayload,
            CustomerStatus customerStatus,
            SagaStatus sagaStatus,
            OutboxStatus outboxStatus,
            UUID sagaId) {

        save(CustomerOutboxMessage.builder()
                .id(UUID.randomUUID())
                .sagaId(sagaId)
                .createdAt(customerEventPayload.getCreatedAt())
                .type(CUSTOMER_SAGA_NAME)
                .payload(createPayload(customerEventPayload))
                .customerStatus(customerStatus)
                .sagaStatus(sagaStatus)
                .outboxStatus(outboxStatus)
                .build());
    }

    @Transactional
    private void save(CustomerOutboxMessage customerOutboxMessage) {
        CustomerOutboxMessage response = customerOutboxRepository.save(customerOutboxMessage);
        if (response == null) {
            log.error("Could not save OrderPaymentOutboxMessage with outbox with id {}", customerOutboxMessage.getId());
            throw new CustomerDomainException("Could not save OrderPaymentOutboxMessage with outbox with id "
                    + customerOutboxMessage.getId());
        }
        log.info("OrderPaymentOutboxMessage saved with outbox id {}", customerOutboxMessage.getId());
    }

    private String createPayload(CustomerEventPayload customerEventPayload) {
        try {
            return objectMapper.writeValueAsString(customerEventPayload);
        } catch (JsonProcessingException e) {
            log.error("Could not create CustomerEventPayload object for order id {}",
                    customerEventPayload.getCustomerId(), e);
            throw new CustomerDomainException("Could not create CustomerEventPayload object for order id " +
                    customerEventPayload.getCustomerId(), e);
        }
    }
}
