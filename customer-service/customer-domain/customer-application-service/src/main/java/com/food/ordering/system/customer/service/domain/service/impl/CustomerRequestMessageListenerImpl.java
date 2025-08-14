package com.food.ordering.system.customer.service.domain.service.impl;

import com.food.ordering.system.customer.service.domain.dto.message.CustomerResponse;
import com.food.ordering.system.customer.service.domain.ports.input.message.listener.CustomerRequestMessageListener;
import com.food.ordering.system.customer.service.domain.service.helper.CustomerRequestHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CustomerRequestMessageListenerImpl implements CustomerRequestMessageListener {

    private final CustomerRequestHelper customerRequestHelper;

    public CustomerRequestMessageListenerImpl(CustomerRequestHelper customerRequestHelper) {
        this.customerRequestHelper = customerRequestHelper;
    }

    @Override
    public void completeCustomer(CustomerResponse customerResponse) {
        customerRequestHelper.process(customerResponse);
    }
}
