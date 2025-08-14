package com.food.ordering.system.customer.service.domain.dto.create;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotBlank;

@Getter
@Builder
public class CreateCustomerCommand {

    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @NotBlank
    private String username;

    @JsonCreator
    public CreateCustomerCommand(
            @JsonProperty("firstName") String firstName,
            @JsonProperty("lastName") String lastName,
            @JsonProperty("username") String username) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
    }
}
