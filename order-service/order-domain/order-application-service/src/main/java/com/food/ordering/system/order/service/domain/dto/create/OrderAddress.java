package com.food.ordering.system.order.service.domain.dto.create;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;

@Getter
@Builder
@AllArgsConstructor
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
}
