package com.food.ordering.system.customer.service.domain.service.customer;

import com.food.ordering.system.customer.service.domain.dto.create.CreateCustomerCommand;
import com.food.ordering.system.customer.service.domain.event.CustomerCreatedEvent;
import com.food.ordering.system.customer.service.domain.service.helper.CustomerCreateHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ExecutionException;

@Slf4j
@Component
public class CustomerCreateCommandHandler {

    private final CustomerCreateHelper customerCreateHelper;

    public CustomerCreateCommandHandler(CustomerCreateHelper customerCreateHelper) {
        this.customerCreateHelper = customerCreateHelper;
    }

    @Transactional
    public CustomerCreatedEvent createCustomer(CreateCustomerCommand createCustomerCommand) throws ExecutionException, InterruptedException {
        customerCreateHelper.verifyCustomerExistsByUsername(createCustomerCommand.getUsername());
        CustomerCreatedEvent customerEvent = customerCreateHelper.persistCustomer(createCustomerCommand);
        log.info("Returning CreateCustomerResponse with order id {}", customerEvent.getCustomer().getId().getValue().toString());
        return customerEvent;
    }
}
