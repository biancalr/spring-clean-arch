package com.food.ordering.system.restaurant.service.domain.outbox.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Builder
public class OrderEventPayload {
    @JsonProperty
    private String orderId;

    @JsonProperty
    private String restaurantId;

    @JsonProperty
    private ZonedDateTime createdAt;

    @JsonProperty
    private String orderApprovalStatus;

    @JsonProperty
    private List<String> failureMessages;

    @JsonCreator
    public OrderEventPayload(
            @JsonProperty("orderId") String orderId,
            @JsonProperty("restaurantId") String restaurantId,
            @JsonProperty("createdAt") ZonedDateTime createdAt,
            @JsonProperty("orderApprovalStatus") String orderApprovalStatus,
            @JsonProperty("failureMessages") List<String> failureMessages) {
        this.orderId = orderId;
        this.restaurantId = restaurantId;
        this.createdAt = createdAt;
        this.orderApprovalStatus = orderApprovalStatus;
        this.failureMessages = failureMessages;
    }
}
