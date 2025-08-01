package com.food.ordering.system.order.service.domain.saga;

import com.food.ordering.system.domain.valueobject.OrderStatus;
import com.food.ordering.system.domain.valueobject.PaymentStatus;
import com.food.ordering.system.order.service.domain.dto.message.PaymentResponse;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalOutboxMessage;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.outbox.scheduler.approval.ApprovalOutboxHelper;
import com.food.ordering.system.order.service.domain.outbox.scheduler.payment.PaymentOutboxHelper;
import com.food.ordering.system.order.service.domain.service.OrderDomainService;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import com.food.ordering.system.saga.SagaStep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static com.food.ordering.system.domain.constant.DomainConstants.UTC;

@Slf4j
@Component
public class OrderPaymentSaga implements SagaStep<PaymentResponse> {

    private final OrderDomainService orderDomainService;
    private final PaymentOutboxHelper paymentOutboxHelper;
    private final ApprovalOutboxHelper approvalOutboxHelper;
    private final OrderSagaRepositoryHelper orderSagaHelper;
    private final OrderDataMapper orderDataMapper;

    public OrderPaymentSaga(OrderDomainService orderDomainService,
                            PaymentOutboxHelper paymentOutboxHelper,
                            ApprovalOutboxHelper approvalOutboxHelper,
                            OrderSagaRepositoryHelper orderSagaHelper, OrderDataMapper orderDataMapper) {
        this.orderDomainService = orderDomainService;
        this.paymentOutboxHelper = paymentOutboxHelper;
        this.approvalOutboxHelper = approvalOutboxHelper;
        this.orderSagaHelper = orderSagaHelper;
        this.orderDataMapper = orderDataMapper;
    }

    @Override
    @Transactional
    public void process(PaymentResponse paymentResponse) {
        final var paymentOutboxMessageResponse = paymentOutboxHelper.getPaymentOutboxMessageBySagaIdAndSagaStatus
                (UUID.fromString(paymentResponse.getSagaId()),
                        SagaStatus.STARTED);
        if (paymentOutboxMessageResponse.isEmpty()) {
            log.info("For order id {} an outbox message with saga id {} is already processed",
                    paymentResponse.getOrderId(), paymentResponse.getSagaId());
            return;
        }
        final var orderPaymentOutboxMessage = paymentOutboxMessageResponse.get();
        final var domainEvent = completeOrderPaidEvent(paymentResponse);
        final var sagaStatus = orderSagaHelper.orderStatusToSagaStatus(domainEvent.getOrder().getOrderStatus());
        paymentOutboxHelper
                .save(getUpdatedPaymentOutboxMessage(
                        orderPaymentOutboxMessage,
                        domainEvent.getOrder().getOrderStatus(),
                        sagaStatus));
        approvalOutboxHelper
                .saveApprovalOutboxMessage(
                        orderDataMapper.orderPaidEventToOrderApprovalEventPayload(domainEvent),
                        domainEvent.getOrder().getOrderStatus(),
                        sagaStatus, OutboxStatus.STARTED,
                        UUID.fromString(paymentResponse.getSagaId()));
        log.info("Order with id {} is paid", domainEvent.getOrder().getId().getValue());
    }

    @Override
    @Transactional
    public void rollback(PaymentResponse paymentResponse) {
        final var paymentOutboxMessageResponse =
                paymentOutboxHelper.getPaymentOutboxMessageBySagaIdAndSagaStatus
                        (UUID.fromString(paymentResponse.getSagaId()),
                                getcurrentSagaStatus(paymentResponse.getPaymentStatus()));
        if (paymentOutboxMessageResponse.isEmpty()) {
            log.info("For order id {} an outbox message with saga id {} is already rolled back",
                    paymentResponse.getOrderId(), paymentResponse.getSagaId());
            return;
        }
        final var orderPaymentOutboxMessage = paymentOutboxMessageResponse.get();
        final var order = rollbackPaymentOrder(paymentResponse);
        final var sagaStatus = orderSagaHelper.orderStatusToSagaStatus(order.getOrderStatus());
        paymentOutboxHelper.save(
                getUpdatedPaymentOutboxMessage(
                        orderPaymentOutboxMessage,
                        order.getOrderStatus(),
                        sagaStatus));
        if (paymentResponse.getPaymentStatus() == PaymentStatus.CANCELLED) {
            approvalOutboxHelper.save(getUpdatedApprovalOutboxMessage(
                    paymentResponse.getSagaId(),
                    order.getOrderStatus(),
                    sagaStatus
            ));
        }
        log.info("Order with id {} is cancelled", order.getId().getValue());
    }

    private OrderPaymentOutboxMessage getUpdatedPaymentOutboxMessage
            (OrderPaymentOutboxMessage orderPaymentOutboxMessage,
             OrderStatus orderStatus, SagaStatus sagaStatus) {
        orderPaymentOutboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of(UTC)));
        orderPaymentOutboxMessage.setOrderStatus(orderStatus);
        orderPaymentOutboxMessage.setSagaStatus(sagaStatus);
        return orderPaymentOutboxMessage;
    }

    private OrderPaidEvent completeOrderPaidEvent
            (PaymentResponse paymentResponse) {
        log.info("Completing payment for order with id {}", paymentResponse.getOrderId());
        final var order = orderSagaHelper.findOrder(paymentResponse.getOrderId());
        final var domainEvent = orderDomainService.payOrder(order);
        orderSagaHelper.saveOrder(order);
        return domainEvent;
    }

    private SagaStatus[] getcurrentSagaStatus(PaymentStatus paymentStatus) {
        return switch (paymentStatus) {
            case COMPLETED -> new SagaStatus[]{SagaStatus.STARTED};
            case CANCELLED -> new SagaStatus[]{SagaStatus.PROCESSING};
            case FAILED -> new SagaStatus[]{SagaStatus.STARTED, SagaStatus.PROCESSING};
        };
    }

    private Order rollbackPaymentOrder(PaymentResponse paymentResponse) {
        log.info("Cancelling order with id {}", paymentResponse.getOrderId());
        final var order = orderSagaHelper.findOrder(paymentResponse.getOrderId());
        orderDomainService.cancelOrder(order, paymentResponse.getFailureMessages());
        orderSagaHelper.saveOrder(order);
        return order;
    }

    private OrderApprovalOutboxMessage getUpdatedApprovalOutboxMessage(String sagaId, OrderStatus orderStatus, SagaStatus sagaStatus) {
        Optional<OrderApprovalOutboxMessage> orderApprovalOutboxMessageResponse =
                approvalOutboxHelper.getApprovalOutboxMessageBySagaIdAndSagaStatus(
                        UUID.fromString(sagaId),
                        SagaStatus.COMPENSATING);
        if (orderApprovalOutboxMessageResponse.isEmpty()) {
            throw new OrderDomainException(
                    "Approval outbox message could not be found in " +
                    SagaStatus.COMPENSATING.name() +
                    " status");
        }
        final var orderApprovalOutboxMessage =
                orderApprovalOutboxMessageResponse.get();
        orderApprovalOutboxMessage.setProcessedAt(ZonedDateTime.now(ZoneId.of(UTC)));
        orderApprovalOutboxMessage.setOrderStatus(orderStatus);
        orderApprovalOutboxMessage.setSagaStatus(sagaStatus);
        return orderApprovalOutboxMessage;
    }
}
