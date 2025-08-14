package com.food.ordering.system.customer.service.domain.outbox.model;

import com.food.ordering.system.domain.valueobject.CustomerStatus;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class CustomerOutboxMessage {
    private UUID id;
    private UUID sagaId;
    private ZonedDateTime createdAt;
    private ZonedDateTime processedAt;
    private String type;
    private String payload;
    private CustomerStatus customerStatus;
    private OutboxStatus outboxStatus;
    private SagaStatus sagaStatus;
    private int version;

    public void setOutboxStatus(OutboxStatus outboxStatus) {
        this.outboxStatus = outboxStatus;
    }
}
