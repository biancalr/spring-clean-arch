package com.food.ordering.system.payment.service.domain.service.helper;

import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.payment.service.domain.dto.PaymentRequest;
import com.food.ordering.system.payment.service.domain.entity.CreditEntry;
import com.food.ordering.system.payment.service.domain.entity.CreditHistory;
import com.food.ordering.system.payment.service.domain.entity.Payment;
import com.food.ordering.system.payment.service.domain.event.PaymentEvent;
import com.food.ordering.system.payment.service.domain.exception.PaymentApplicationServiceException;
import com.food.ordering.system.payment.service.domain.mapper.PaymentDataMapper;
import com.food.ordering.system.payment.service.domain.ports.output.message.publisher.PaymentCancelledMessagePublisher;
import com.food.ordering.system.payment.service.domain.ports.output.message.publisher.PaymentCompletedMessagePublisher;
import com.food.ordering.system.payment.service.domain.ports.output.message.publisher.PaymentFailedMessagePublisher;
import com.food.ordering.system.payment.service.domain.ports.output.repository.CreditEntryRepository;
import com.food.ordering.system.payment.service.domain.ports.output.repository.CreditHistoryRepository;
import com.food.ordering.system.payment.service.domain.ports.output.repository.PaymentRepository;
import com.food.ordering.system.payment.service.domain.service.PaymentDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class PaymentRequestHelper {

    private final PaymentDomainService paymentDomainService;
    private final PaymentDataMapper paymentDataMapper;
    private final PaymentRepository paymentRepository;
    private final CreditEntryRepository creditEntryRepository;
    private final CreditHistoryRepository creditHistoryRepository;
    private final PaymentCompletedMessagePublisher paymentCompletedEventDomainEventPublisher;
    private final PaymentCancelledMessagePublisher paymentCancelledEventDomainEventPublisher;
    private final PaymentFailedMessagePublisher paymentFailedEventDomainEventPublisher;

    public PaymentRequestHelper(PaymentDomainService paymentDomainService,
                                PaymentDataMapper paymentDataMapper,
                                PaymentRepository paymentRepository,
                                CreditEntryRepository creditEntryRepository,
                                CreditHistoryRepository creditHistoryRepository, PaymentCompletedMessagePublisher paymentCompletedEventDomainEventPublisher, PaymentCancelledMessagePublisher paymentCancelledEventDomainEventPublisher, PaymentFailedMessagePublisher paymentFailedEventDomainEventPublisher) {
        this.paymentDomainService = paymentDomainService;
        this.paymentDataMapper = paymentDataMapper;
        this.paymentRepository = paymentRepository;
        this.creditEntryRepository = creditEntryRepository;
        this.creditHistoryRepository = creditHistoryRepository;
        this.paymentCompletedEventDomainEventPublisher = paymentCompletedEventDomainEventPublisher;
        this.paymentCancelledEventDomainEventPublisher = paymentCancelledEventDomainEventPublisher;
        this.paymentFailedEventDomainEventPublisher = paymentFailedEventDomainEventPublisher;
    }

    @Transactional
    public final PaymentEvent persistPayment(PaymentRequest paymentRequest) {
        log.info("Received payment complete event for order id {}", paymentRequest.getOrderId());
        final var payment = paymentDataMapper.paymentRequestToPayment(paymentRequest);
        return persist(payment);
    }

    @Transactional
    public final PaymentEvent persistCancelledPayment(PaymentRequest paymentRequest) {
        log.info("Received payment rollback event for order id {}", paymentRequest.getOrderId());
        final var paymentResponse = paymentRepository
                .findByOrderId(UUID.fromString(paymentRequest.getOrderId()));
        if (paymentResponse.isEmpty()) {
            log.error("Payment with order id {} could not be found", paymentRequest.getOrderId());
            throw new PaymentApplicationServiceException("Payment with order id "
                    + paymentRequest.getOrderId() + " could not be found");
        }

        final var payment = paymentResponse.get();
        return persist(payment);
    }

    private CreditEntry getCreditEntry(final CustomerId customerId) {
        final var creditEntry = creditEntryRepository.findByCustomerId(customerId);
        if (creditEntry.isEmpty()) {
            log.error("Could not find credit for customer: {}", customerId.getValue());
            throw new PaymentApplicationServiceException("Could not find credit for customer " + customerId.getValue());
        }
        return creditEntry.get();
    }

    private List<CreditHistory> getCreditHistory(final CustomerId customerId) {
        final var creditHistory = creditHistoryRepository.findByCustomerId(customerId);
        if (creditHistory.isEmpty()) {
            log.error("Could not find credit history for customer: {}", customerId.getValue());
            throw new PaymentApplicationServiceException("Could not find credit history for customer " + customerId.getValue());
        }
        return creditHistory.get();
    }

    private void persistDbObjects(Payment payment,
                                  List<String> failureMessages,
                                  CreditEntry creditEntry,
                                  List<CreditHistory> creditHistories) {
        paymentRepository.save(payment);
        if (failureMessages.isEmpty()) {
            creditEntryRepository.save(creditEntry);
            creditHistoryRepository.save(
                    creditHistories.get(creditHistories.size() - 1));
        }
    }

    private PaymentEvent persist(final Payment payment) {
        final CreditEntry creditEntry;
        final List<CreditHistory> creditHistories;
        final PaymentEvent paymentEvent;
        final List<String> failureMessages = new ArrayList<>();

        creditEntry = getCreditEntry(payment.getCustomerId());
        creditHistories = getCreditHistory(payment.getCustomerId());

        paymentEvent = switch (payment.getPaymentStatus().name()) {
            case "COMPLETED" ->
                    paymentDomainService.validateAndInitiatePayment(payment, creditEntry, creditHistories, failureMessages, paymentCompletedEventDomainEventPublisher, paymentFailedEventDomainEventPublisher);
            case "CANCELLED" ->
                    paymentDomainService.validateAndCancelEvent(payment, creditEntry, creditHistories, failureMessages, paymentCancelledEventDomainEventPublisher, paymentFailedEventDomainEventPublisher);
            default -> null;
        };

        persistDbObjects(payment, failureMessages, creditEntry, creditHistories);

        return paymentEvent;
    }
}
