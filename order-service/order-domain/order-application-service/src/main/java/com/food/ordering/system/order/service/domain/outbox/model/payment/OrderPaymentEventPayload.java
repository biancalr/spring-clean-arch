package com.food.ordering.system.order.service.domain.outbox.model.payment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Getter
@Builder
public class OrderPaymentEventPayload {
    @JsonProperty
    private String orderId;
    @JsonProperty
    private String customerId;
    @JsonProperty
    private BigDecimal price;
    @JsonProperty
    private ZonedDateTime createdAt;
    @JsonProperty
    private String paymentOrderStatus;

    @JsonCreator
    public OrderPaymentEventPayload(
            @JsonProperty("orderId") String orderId,
            @JsonProperty("customerId") String customerId,
            @JsonProperty("price") BigDecimal price,
            @JsonProperty("createdAt") ZonedDateTime createdAt,
            @JsonProperty("paymentOrderStatus") String paymentOrderStatus) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.price = price;
        this.createdAt = createdAt;
        this.paymentOrderStatus = paymentOrderStatus;
    }
}
