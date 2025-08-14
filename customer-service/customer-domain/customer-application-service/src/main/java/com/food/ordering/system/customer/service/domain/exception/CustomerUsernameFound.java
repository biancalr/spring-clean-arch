package com.food.ordering.system.customer.service.domain.exception;

import com.food.ordering.system.domain.exception.DomainException;

public class CustomerUsernameFound extends DomainException {

    public CustomerUsernameFound(String message, Throwable cause) {
        super(message, cause);
    }

    public CustomerUsernameFound(String message) {
        super(message);
    }
}
