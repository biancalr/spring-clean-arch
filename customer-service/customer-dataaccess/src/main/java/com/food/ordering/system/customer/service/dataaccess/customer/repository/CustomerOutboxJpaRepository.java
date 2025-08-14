package com.food.ordering.system.customer.service.dataaccess.customer.repository;

import com.food.ordering.system.customer.service.dataaccess.customer.entity.CustomerOutboxEntity;
import com.food.ordering.system.outbox.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerOutboxJpaRepository extends JpaRepository<CustomerOutboxEntity, UUID> {
    Optional<List<CustomerOutboxEntity>> findByTypeAndOutboxStatus(String type, OutboxStatus outboxStatus);

    Optional<CustomerOutboxEntity> findByTypeAndSagaIdAndOutboxStatus(String sagaType, UUID sagaId, OutboxStatus outboxStatus);

    void deleteByTypeAndOutboxStatusIn(String type, List<OutboxStatus> outboxStatus);
}
