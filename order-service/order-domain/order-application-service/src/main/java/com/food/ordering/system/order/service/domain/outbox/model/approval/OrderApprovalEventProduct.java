package com.food.ordering.system.order.service.domain.outbox.model.approval;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderApprovalEventProduct {
    @JsonProperty
    private String id;
    @JsonProperty
    private Integer quantity;

    @JsonCreator
    public OrderApprovalEventProduct(
            @JsonProperty("id") String id,
            @JsonProperty("quantity") Integer quantity) {
        this.id = id;
        this.quantity = quantity;
    }
}
