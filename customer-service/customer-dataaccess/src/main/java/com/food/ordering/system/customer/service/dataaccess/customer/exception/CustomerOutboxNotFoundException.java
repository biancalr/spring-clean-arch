package com.food.ordering.system.customer.service.dataaccess.customer.exception;

public class CustomerOutboxNotFoundException extends RuntimeException {
    public CustomerOutboxNotFoundException(String message) {
        super(message);
    }
}
