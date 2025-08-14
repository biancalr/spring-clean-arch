package com.food.ordering.system.customer.service.domain.exception;

import com.food.ordering.system.domain.exception.DomainException;

public class CustomerNotFound extends DomainException {

    public CustomerNotFound(String message) {
        super(message);
    }

    public CustomerNotFound(String message, Throwable cause) {
        super(message, cause);
    }
}
