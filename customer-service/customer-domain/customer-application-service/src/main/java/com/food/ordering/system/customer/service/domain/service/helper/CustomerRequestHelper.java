package com.food.ordering.system.customer.service.domain.service.helper;

import com.food.ordering.system.customer.service.domain.dto.message.CustomerResponse;
import com.food.ordering.system.customer.service.domain.entity.Customer;
import com.food.ordering.system.customer.service.domain.exception.CustomerNotFound;
import com.food.ordering.system.customer.service.domain.mapper.CustomerDataMapper;
import com.food.ordering.system.customer.service.domain.outbox.model.CustomerOutboxMessage;
import com.food.ordering.system.customer.service.domain.outbox.scheduler.CustomerOutboxHelper;
import com.food.ordering.system.customer.service.domain.ports.output.message.publisher.CustomerResponseMessagePublisher;
import com.food.ordering.system.customer.service.domain.ports.output.repository.CustomerRepository;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class CustomerRequestHelper implements SagaStep<CustomerResponse> {

    private final CustomerDataMapper customerDataMapper;
    private final CustomerRepository customerRepository;
    private final CustomerOutboxHelper customerOutboxHelper;
    private final CustomerResponseMessagePublisher customerResponseMessagePublisher;

    public CustomerRequestHelper(
            CustomerDataMapper customerDataMapper,
            CustomerRepository customerRepository,
            CustomerOutboxHelper customerOutboxHelper,
            CustomerResponseMessagePublisher customerResponseMessagePublisher) {
        this.customerDataMapper = customerDataMapper;
        this.customerRepository = customerRepository;
        this.customerOutboxHelper = customerOutboxHelper;
        this.customerResponseMessagePublisher = customerResponseMessagePublisher;
    }

    @Transactional
    public void process(CustomerResponse customerResponse) {
        if (publishIfOutboxMessageProcessedForCustomer(customerResponse)) {
            log.info("An outbox message with saga id: {} is already saved to database!",
                    customerResponse.getSagaId());
            return;
        }
        Customer customer = getCustomer(customerResponse);
        log.info("Registering customer creation for customer id: {}", customer.getId());

        customerOutboxHelper.saveOrderOutboxMessage(
                customerDataMapper.customerToOrderEventPayload(
                        customer,
                        customerResponse.getCreatedAt()),
                customer.getCustomerStatus(),
                OutboxStatus.COMPLETED,
                UUID.fromString(customerResponse.getSagaId()));
    }

    private Customer getCustomer(CustomerResponse customerResponse) {
        Optional<Customer> customer = customerRepository.findById(customerResponse.getId());
        if (customer.isEmpty()) {
            throw new CustomerNotFound("Customer not found");
        }
        return customer.get();
    }

    private boolean publishIfOutboxMessageProcessedForCustomer(
            CustomerResponse createCustomerCommand) {
        Optional<CustomerOutboxMessage> orderOutboxMessage =
                customerOutboxHelper.getCompletedOrderOutboxMessageBySagaIdAndCustomerStatus(
                        UUID.fromString(createCustomerCommand.getSagaId()),
                        OutboxStatus.COMPLETED);
        if (orderOutboxMessage.isPresent()) {
            customerResponseMessagePublisher.publish(
                    orderOutboxMessage.get(),
                    customerOutboxHelper::updateOutboxStatus);
            return true;
        }
        return false;

    }

    @Override
    public void rollback(CustomerResponse data) {
        log.info("Implementation not used");
    }
}
