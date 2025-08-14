package com.food.ordering.system.customer.service.messaging.listener.kafka;

import com.food.ordering.system.customer.service.domain.exception.CustomerApplicationServiceException;
import com.food.ordering.system.customer.service.domain.exception.CustomerNotFound;
import com.food.ordering.system.customer.service.domain.ports.input.message.listener.CustomerRequestMessageListener;
import com.food.ordering.system.customer.service.messaging.mapper.CustomerMessagingDataMapper;
import com.food.ordering.system.kafka.consumer.service.KafkaConsumer;
import com.food.ordering.system.kafka.order.avro.model.CustomerRequestAvroModel;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PSQLState;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;

@Slf4j
@Component
public class CustomerRequestKafkaListener implements KafkaConsumer<CustomerRequestAvroModel> {

    private final CustomerRequestMessageListener customerResponseMessagePublisher;
    private final CustomerMessagingDataMapper customerMessagingDataMapper;

    public CustomerRequestKafkaListener(CustomerRequestMessageListener customerResponseMessagePublisher, CustomerMessagingDataMapper customerMessagingDataMapper) {
        this.customerResponseMessagePublisher = customerResponseMessagePublisher;
        this.customerMessagingDataMapper = customerMessagingDataMapper;
    }

    @KafkaListener(id = "${kafka-consumer-config.customer-consumer-group-id}",
            topics = "${customer-service.customer-request-topic-name}")
    @Override
    public void receive(@Payload List<CustomerRequestAvroModel> messages,
                        @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) List<String> keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets) {
        log.info("{} number of customer requests received with keys {}, partitions {} and offsets {}" +
                        ", sending for customer approval",
                messages.size(),
                keys.toString(),
                partitions.toString(),
                offsets.toString());

        messages.forEach(this::fireEvent);
    }

    private void fireEvent(CustomerRequestAvroModel customerRequestAvroModel) {
        try {
            log.info("Processing order approval for user: {}", customerRequestAvroModel.getId());
            customerResponseMessagePublisher.completeCustomer(
                    customerMessagingDataMapper.
                            customerRequestAvroModelResponseAvroModeloCustomerResponse(customerRequestAvroModel));
        } catch (DataAccessException e) {
            SQLException sqlException = (SQLException) e.getRootCause();
            if (sqlException != null && sqlException.getSQLState() != null &&
                    PSQLState.UNIQUE_VIOLATION.getState().equals(sqlException.getSQLState())) {
                //NO-OP for unique constraint exception
                log.error("Caught unique constraint exception with sql state: {} " +
                                "in CustomerRequestKafkaListener for saga id: {}",
                        sqlException.getSQLState(), customerRequestAvroModel.getSagaId());
            } else {
                throw new CustomerApplicationServiceException("Throwing DataAccessException in" +
                        " CustomerRequestKafkaListener: " + e.getMessage(), e);
            }
        } catch (CustomerNotFound e) {
            log.error("Customer not found for {}", customerRequestAvroModel.getId());
        }
    }
}
