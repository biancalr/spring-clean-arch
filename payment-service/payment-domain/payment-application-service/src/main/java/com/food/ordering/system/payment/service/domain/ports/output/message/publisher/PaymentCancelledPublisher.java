package com.food.ordering.system.payment.service.domain.ports.output.message.publisher;

import com.food.ordering.system.domain.event.publisher.DomainEventPublisher;
import com.food.ordering.system.payment.service.domain.event.PaymentCancelled;

public interface PaymentCancelledPublisher extends DomainEventPublisher<PaymentCancelled> {
}
