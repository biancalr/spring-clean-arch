package com.food.ordering.system.customer.service.domain.ports.output.repository;

import com.food.ordering.system.customer.service.domain.outbox.model.CustomerOutboxMessage;
import com.food.ordering.system.outbox.OutboxStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerOutboxRepository {
    CustomerOutboxMessage save(CustomerOutboxMessage customerOutboxMessage);

    Optional<List<CustomerOutboxMessage>> findByTypeAndOutboxStatus(String type, OutboxStatus outboxStatus);

    Optional<CustomerOutboxMessage> findByTypeAndSagaIdAndOutboxStatus(String type, UUID sagaId,
                                                                             OutboxStatus outboxStatus);

    void deleteByTypeAndOutboxStatus(String type, OutboxStatus... outboxStatus);
}
