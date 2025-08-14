package com.food.ordering.system.customer.service.domain.dto.create;

import com.food.ordering.system.domain.valueobject.CustomerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class CreateCustomerResponse {

    private final UUID customerId;
    private final CustomerStatus customerStatus;
    private final String firstName;
    private final String lastName;
    private final String username;
    private final String message;
}
