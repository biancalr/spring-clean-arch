package com.food.ordering.system.customer.service.domain.dto.create;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class CreateCustomerResponse {

    @NotBlank
    private final UUID customerId;
    @NotBlank
    private final String message;
}
