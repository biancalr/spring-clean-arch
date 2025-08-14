package com.food.ordering.system.customer.service.domain.service.customer;

import com.food.ordering.system.customer.service.domain.dto.create.CreateCustomerCommand;
import com.food.ordering.system.customer.service.domain.dto.create.CreateCustomerResponse;
import com.food.ordering.system.customer.service.domain.ports.input.service.CustomerApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Validated
@Service
public class CustomerApplicationServiceImpl implements CustomerApplicationService {

    private final CustomerCreateCommandHandler customerCreateCommandHandler;

    public CustomerApplicationServiceImpl(
            CustomerCreateCommandHandler customerCreateCommandHandler) {
        this.customerCreateCommandHandler = customerCreateCommandHandler;
    }

    @Override
    public CreateCustomerResponse createCustomer(CreateCustomerCommand createCustomerCommand) {
        return customerCreateCommandHandler.createCustomer(createCustomerCommand);
    }
}
