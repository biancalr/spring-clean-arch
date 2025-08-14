package com.food.ordering.system.customer.service.domain.ports.input.message.listener;

import com.food.ordering.system.customer.service.domain.dto.message.CustomerResponse;

public interface CustomerRequestMessageListener {
    void completeCustomer(CustomerResponse customerResponse);
}
