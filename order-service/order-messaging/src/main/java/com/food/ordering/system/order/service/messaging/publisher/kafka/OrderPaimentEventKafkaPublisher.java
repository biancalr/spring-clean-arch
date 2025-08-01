package com.food.ordering.system.order.service.messaging.publisher.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.kafka.producer.service.helper.KafkaMessageHelper;
import com.food.ordering.system.order.service.domain.config.OrderServiceConfigData;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentEventPayload;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.port.output.message.publisher.payment.PaymentRequestMessagePublisher;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import com.food.ordering.system.outbox.OutboxStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;

@Slf4j
@Component
public class OrderPaimentEventKafkaPublisher implements PaymentRequestMessagePublisher {

    private final OrderMessagingDataMapper orderMessagingDataMapper;
    private final KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer;
    private final OrderServiceConfigData orderServiceConfigData;
    private final KafkaMessageHelper kafkaMessageHelper;
    private final ObjectMapper objectMapper;

    public OrderPaimentEventKafkaPublisher(OrderMessagingDataMapper orderMessagingDataMapper,
                                           KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer,
                                           OrderServiceConfigData orderServiceConfigData,
                                           KafkaMessageHelper kafkaMessageHelper,
                                           ObjectMapper objectMapper) {
        this.orderMessagingDataMapper = orderMessagingDataMapper;
        this.kafkaProducer = kafkaProducer;
        this.orderServiceConfigData = orderServiceConfigData;
        this.kafkaMessageHelper = kafkaMessageHelper;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(OrderPaymentOutboxMessage orderPaymentOutboxMessage,
                        BiConsumer<OrderPaymentOutboxMessage, OutboxStatus> outboxCallback) {
        final var orderPaymentEventPayload =
                getOrderPaymentEventPayload(orderPaymentOutboxMessage.getPayload());
        final var sagaId = orderPaymentOutboxMessage.getSagaId().toString();
        log.info("Received OrderPaymentOutboxMessage for order id {} and saga id {}",
                orderPaymentEventPayload.getOrderId(),
                sagaId);
        try {
            final var paymentResponseAvroModel =
                    orderMessagingDataMapper.orderPaymentEventToPaymentRequestAvroModel(
                            sagaId, orderPaymentEventPayload);
            kafkaProducer.send(
                    orderServiceConfigData.getPaymentRequestTopicName(),
                    sagaId,
                    paymentResponseAvroModel,
                    kafkaMessageHelper.getKafkaCallback(
                            orderServiceConfigData.getPaymentRequestTopicName(),
                            paymentResponseAvroModel,
                            orderPaymentOutboxMessage,
                            outboxCallback,
                            orderPaymentEventPayload.getOrderId(),
                            "PaymentRequestAvroModel"));
            log.info(
                    "OrderPaymentEventPayload sent to kafka for order id {} and saga id {}",
                    orderPaymentEventPayload.getOrderId(),
                    sagaId);
        } catch (Exception e) {
            log.error("Error while sending OrderPaymentEventPayload" +
                            " to kafka with order id: {} and saga id: {}, error: {}",
                    orderPaymentEventPayload.getOrderId(),
                    sagaId,
                    e.getMessage());
        }

    }

    private OrderPaymentEventPayload getOrderPaymentEventPayload(String payload) {
        try {
            return objectMapper.readValue(payload,
                    OrderPaymentEventPayload.class);
        } catch (JsonProcessingException e) {
            log.error("Could not read OrderPaymentEventPayload object", e);
            throw new OrderDomainException("Could not read OrderPaymentEventPayload object", e);
        }
    }
}
