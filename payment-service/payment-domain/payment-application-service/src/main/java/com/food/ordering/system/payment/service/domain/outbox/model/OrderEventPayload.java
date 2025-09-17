package com.food.ordering.system.payment.service.domain.outbox.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Builder
public class OrderEventPayload {
    @JsonProperty
    private String paymentId;

    @JsonProperty
    private String customerId;

    @JsonProperty
    private String orderId;

    @JsonProperty
    private BigDecimal price;

    @JsonProperty
    private ZonedDateTime createdAt;

    @JsonProperty
    private String paymentStatus;

    @JsonProperty
    private List<String> failureMessages;

    @JsonCreator
    public OrderEventPayload(
            @JsonProperty("paymentId") String paymentId,
            @JsonProperty("customerId") String customerId,
            @JsonProperty("orderId") String orderId,
            @JsonProperty("price") BigDecimal price,
            @JsonProperty("createdAt") ZonedDateTime createdAt,
            @JsonProperty("paymentStatus") String paymentStatus,
            @JsonProperty("failureMessages") List<String> failureMessages) {
        this.paymentId = paymentId;
        this.customerId = customerId;
        this.orderId = orderId;
        this.price = price;
        this.createdAt = createdAt;
        this.paymentStatus = paymentStatus;
        this.failureMessages = failureMessages;
    }
}
