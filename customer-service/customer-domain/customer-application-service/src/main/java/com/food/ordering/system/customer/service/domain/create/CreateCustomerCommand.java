package com.food.ordering.system.customer.service.domain.create;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Getter
@Builder
public class CreateCustomerCommand {
    @NotNull
    private final UUID customerId;
    @NotNull
    private final String username;
    @NotNull
    private final String firstName;
    @NotNull
    private final String lastName;

    @JsonCreator
    public CreateCustomerCommand(
            @NotNull UUID customerId,
            @NotNull String username,
            @NotNull String firstName,
            @NotNull String lastName) {
        this.customerId = customerId;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
