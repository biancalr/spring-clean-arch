package com.food.ordering.system.customer.service.messaging.publisher.kafka;

import com.food.ordering.system.customer.service.domain.config.CustomerServiceConfigData;
import com.food.ordering.system.customer.service.domain.outbox.model.CustomerOutboxMessage;
import com.food.ordering.system.customer.service.domain.outbox.model.CustomerEventPayload;
import com.food.ordering.system.customer.service.domain.ports.output.message.publisher.CustomerResponseMessagePublisher;
import com.food.ordering.system.customer.service.messaging.mapper.CustomerMessagingDataMapper;
import com.food.ordering.system.kafka.order.avro.model.CustomerRequestAvroModel;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.kafka.producer.service.helper.KafkaMessageHelper;
import com.food.ordering.system.outbox.OutboxStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;

@Slf4j
@Component
public class CustomerResponseCreatedEventKafkaPublisher implements CustomerResponseMessagePublisher {

    private final CustomerMessagingDataMapper customerMessagingDataMapper;
    private final KafkaProducer<String, CustomerRequestAvroModel> kafkaProducer;
    private final CustomerServiceConfigData customerServiceConfigData;
    private final KafkaMessageHelper kafkaMessageHelper;

    public CustomerResponseCreatedEventKafkaPublisher(
            CustomerMessagingDataMapper customerMessagingDataMapper,
            KafkaProducer<String, CustomerRequestAvroModel> kafkaProducer,
            CustomerServiceConfigData customerServiceConfigData,
            KafkaMessageHelper kafkaMessageHelper) {
        this.customerMessagingDataMapper = customerMessagingDataMapper;
        this.kafkaProducer = kafkaProducer;
        this.customerServiceConfigData = customerServiceConfigData;
        this.kafkaMessageHelper = kafkaMessageHelper;
    }

    @Override
    public void publish(
            CustomerOutboxMessage customerOutboxMessage,
            BiConsumer<CustomerOutboxMessage, OutboxStatus> outboxCallback) {
        CustomerEventPayload customerEventPayload =
                kafkaMessageHelper.getOrderEventPayload(
                        customerOutboxMessage.getPayload(),
                        CustomerEventPayload.class);
        String sagaId = customerOutboxMessage.getSagaId().toString();

        try {
            CustomerRequestAvroModel customerRequestAvroModel = customerMessagingDataMapper
                    .orderEventPayloadToAvroModelToCustomerApproval(
                            sagaId,
                            customerEventPayload);

            kafkaProducer.send(
                    customerServiceConfigData.getCustomerRequestTopicName(),
                    sagaId,
                    customerRequestAvroModel,
                    kafkaMessageHelper.getKafkaCallback(
                            customerServiceConfigData.getCustomerRequestTopicName(),
                            customerRequestAvroModel,
                            customerOutboxMessage,
                            outboxCallback,
                            sagaId,
                            "CustomerRequestAvroModel"
                    ));

            log.info("CustomerEvent sent to kafka for customer id: {}",
                    customerRequestAvroModel.getId());
        } catch (Exception e) {
            log.error("Error while sending RestaurantApprovalResponseAvroModel message" +
                            " to kafka with saga id: {}, error: {}",
                    sagaId, e.getMessage());
        }
    }
}
