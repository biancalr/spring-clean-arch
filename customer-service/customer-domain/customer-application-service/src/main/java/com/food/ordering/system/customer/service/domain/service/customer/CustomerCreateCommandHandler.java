package com.food.ordering.system.customer.service.domain.service.customer;

import com.food.ordering.system.customer.service.domain.dto.create.CreateCustomerCommand;
import com.food.ordering.system.customer.service.domain.dto.create.CreateCustomerResponse;
import com.food.ordering.system.customer.service.domain.event.CustomerCreatedEvent;
import com.food.ordering.system.customer.service.domain.mapper.CustomerDataMapper;
import com.food.ordering.system.customer.service.domain.outbox.scheduler.CustomerOutboxScheduler;
import com.food.ordering.system.customer.service.domain.saga.CustomerSagaRepositoryHelper;
import com.food.ordering.system.outbox.OutboxStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
public class CustomerCreateCommandHandler {

    private final CustomerCreateHelper customerCreateHelper;
    private final CustomerDataMapper customerDataMapper;
    private final CustomerSagaRepositoryHelper customerSagaRepositoryHelper;
    private final CustomerOutboxScheduler customerOutboxScheduler;

    public CustomerCreateCommandHandler(CustomerCreateHelper customerCreateHelper, CustomerDataMapper customerDataMapper, CustomerSagaRepositoryHelper customerSagaRepositoryHelper, CustomerOutboxScheduler customerOutboxScheduler) {
        this.customerCreateHelper = customerCreateHelper;
        this.customerDataMapper = customerDataMapper;
        this.customerSagaRepositoryHelper = customerSagaRepositoryHelper;
        this.customerOutboxScheduler = customerOutboxScheduler;
    }

    @Transactional
    public CreateCustomerResponse createCustomer(CreateCustomerCommand createCustomerCommand) {
        customerCreateHelper.verifyCustomerExistsByUsername(createCustomerCommand.getUsername());
        CustomerCreatedEvent customerEvent = customerCreateHelper.persistCustomer(createCustomerCommand);
        CreateCustomerResponse createCustomerResponse = customerDataMapper
                .customerToCreateCustomerResponse(customerEvent.getCustomer(), "Customer created successfully");
        customerOutboxScheduler.customerRegisterOutboxMessage(
                customerDataMapper.customerEventToOrderEventPayload(customerEvent),
                customerEvent.getCustomer().getCustomerStatus(),
                customerSagaRepositoryHelper.customerStatusToSagaStatus(customerEvent.getCustomer().getCustomerStatus()),
                OutboxStatus.STARTED,
                UUID.randomUUID()
        );
        log.info("Returning CreateCustomerResponse with order id {}", customerEvent.getCustomer().getId().getValue().toString());
        return createCustomerResponse;
    }
}
