package com.food.ordering.system.customer.service.domain.outbox.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Builder
public class CustomerEventPayload {

    @JsonProperty
    private String customerId;

    @JsonProperty
    private ZonedDateTime createdAt;

    @JsonProperty
    private String customerStatus;

    @JsonProperty
    private String username;

    @JsonProperty
    private String firstName;

    @JsonProperty
    private String lastName;

    @JsonCreator
    public CustomerEventPayload(
            @JsonProperty("customerId") String customerId,
            @JsonProperty("createdAt") ZonedDateTime createdAt,
            @JsonProperty("customerStatus") String customerStatus,
            @JsonProperty("username") String username,
            @JsonProperty("firstName") String firstName,
            @JsonProperty("lastName") String lastName) {
        this.customerId = customerId;
        this.createdAt = createdAt;
        this.customerStatus = customerStatus;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
