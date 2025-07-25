package com.food.ordering.system.order.service.messaging.publisher.kafka;

import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.kafka.producer.service.helper.KafkaMessageHelper;
import com.food.ordering.system.order.service.domain.config.OrderServiceConfigData;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.port.output.message.publisher.payment.OrderCreatedPaymentRequestMessagePublisher;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagindDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CreateOrderKafkaMessagePublisher implements OrderCreatedPaymentRequestMessagePublisher {

    private final OrderMessagindDataMapper orderMessagindDataMapper;
    private final OrderServiceConfigData orderServiceConfigData;
    private final KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer;
    private final KafkaMessageHelper orderKafkaMessageHelper;

    public CreateOrderKafkaMessagePublisher(OrderMessagindDataMapper orderMessagindDataMapper,
                                            OrderServiceConfigData orderServiceConfigData,
                                            KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer, KafkaMessageHelper orderKafkaMessageHelper) {
        this.orderMessagindDataMapper = orderMessagindDataMapper;
        this.orderServiceConfigData = orderServiceConfigData;
        this.kafkaProducer = kafkaProducer;
        this.orderKafkaMessageHelper = orderKafkaMessageHelper;
    }

    @Override
    public void publish(OrderCreatedEvent domainEvent) {
        final String orderId = domainEvent.getOrder().getId().toString();
        log.info("Received OrderCreatedEvent for order id: {}", orderId);

        try {
            final var paymentRequestAvroModel = orderMessagindDataMapper
                    .orderCreatedEventToPaymentRequestApprovalModel(domainEvent);

            kafkaProducer.send(orderServiceConfigData.getPaymentRequestTopicName(),
                    orderId,
                    paymentRequestAvroModel,
                    orderKafkaMessageHelper.getKafkaCallback(
                            orderServiceConfigData.getPaymentResponseTopicName(),
                            paymentRequestAvroModel,
                            "PaymentRequestAvroModel",
                            orderId)
            );
            log.info("PaymentRequestAvroModel sent to Kafka for order id: {}",
                    paymentRequestAvroModel.getOrderId());
        } catch (Exception e) {
            log.error("Error while sending PaymentRequestAvroModel message" +
                    " to kafka with order id: {}, error: {}", orderId, e.getMessage());
        }
    }

}
