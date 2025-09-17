package com.food.ordering.system.order.service.domain.dto.create;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;

@Getter
@Builder
public class OrderAddress {

    @NotBlank
    @Max(value = 50)
    private final String street;
    @NotBlank
    @Max(value = 10)
    private final String postalCode;
    @NotBlank
    @Max(value = 50)
    private final String city;

    @JsonCreator
    public OrderAddress(
            @JsonProperty("street") String street,
            @JsonProperty("postalCode") String postalCode,
            @JsonProperty("city") String city) {
        this.street = street;
        this.postalCode = postalCode;
        this.city = city;
    }
}
