package com.food.ordering.system.customer.service.domain.dto.message;

import com.food.ordering.system.domain.valueobject.CustomerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class CustomerResponse {
    private String id;
    private String sagaId;
    private Instant createdAt;
    private CustomerStatus customerStatus;
    private final List<String> failureMessages;
}
