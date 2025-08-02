package com.food.ordering.system.customer.service.domain.create;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Getter
@Builder
public class CreateCustomerResponse {
    @NotNull
    private final UUID customerId;
    @NotNull
    private final String message;

    @JsonCreator
    public CreateCustomerResponse(
            @NotNull UUID customerId,
            @NotNull String message) {
        this.customerId = customerId;
        this.message = message;
    }
}
