package com.food.ordering.system.payment.service.domain.event;

import com.food.ordering.system.payment.service.domain.entity.Payment;

import java.time.ZonedDateTime;
import java.util.Collections;

public class PaymentCancelled extends PaymentEvent {

    public PaymentCancelled(Payment payment, ZonedDateTime createdAt) {
        super(payment, createdAt, Collections.emptyList());
    }
}
