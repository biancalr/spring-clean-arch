package com.food.ordering.system.customer.service.domain.service.helper;

import com.food.ordering.system.customer.service.domain.dto.create.CreateCustomerCommand;
import com.food.ordering.system.customer.service.domain.entity.Customer;
import com.food.ordering.system.customer.service.domain.event.CustomerCreatedEvent;
import com.food.ordering.system.customer.service.domain.event.CustomerEvent;
import com.food.ordering.system.customer.service.domain.exception.CustomerDomainException;
import com.food.ordering.system.customer.service.domain.exception.CustomerUsernameFound;
import com.food.ordering.system.customer.service.domain.mapper.CustomerDataMapper;
import com.food.ordering.system.customer.service.domain.ports.output.repository.CustomerRepository;
import com.food.ordering.system.customer.service.domain.service.CustomerDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class CustomerCreateHelper {

    private final CustomerRepository customerRepository;
    private final CustomerDataMapper customerDataMapper;
    private final CustomerDomainService customerDomainService;

    public CustomerCreateHelper(CustomerRepository customerRepository,
                                CustomerDataMapper customerDataMapper,
                                CustomerDomainService customerDomainService) {
        this.customerRepository = customerRepository;
        this.customerDataMapper = customerDataMapper;
        this.customerDomainService = customerDomainService;
    }

    @Transactional
    public CustomerCreatedEvent persistCustomer(CreateCustomerCommand createCustomerCommand) throws ExecutionException, InterruptedException {
        final var customer = customerDataMapper.createCustomerCommandToCustomer(createCustomerCommand);
        final var customerEvent = customerDomainService.validateAndInitiateCustomer(customer);
        final var savedCustomer = saveCustomer(customerEvent);
        log.info("Customer is created with id: {}", savedCustomer.getId().getValue());
        return customerEvent;
    }

    private Customer saveCustomer(CustomerEvent customerCreatedEvent) throws ExecutionException, InterruptedException {
        return CompletableFuture.completedFuture(customerRepository.save(customerCreatedEvent.getCustomer()))
                .orTimeout(4000, TimeUnit.SECONDS)
                .whenComplete((c, t) -> {
                    if (c == null) {
                        log.error("Could not save customer");
                        throw new CustomerDomainException("Could not save customer");
                    }
                    log.info("Customer is saved with id: {}", c.getId().getValue());
                }).get();
    }

    public void verifyCustomerExistsByUsername(String username) {
        boolean customerExistsResult = customerRepository.customerExistsByUsername(username);
        if (customerExistsResult) {
            log.error("Username {} already in use", username);
            throw new CustomerUsernameFound("Username already in use");
        }
    }
}
