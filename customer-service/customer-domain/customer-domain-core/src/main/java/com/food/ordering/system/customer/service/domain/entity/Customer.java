package com.food.ordering.system.customer.service.domain.entity;

import com.food.ordering.system.customer.service.domain.exception.CustomerDomainException;
import com.food.ordering.system.domain.entity.AggregateRoot;
import com.food.ordering.system.domain.valueobject.CustomerId;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import static com.food.ordering.system.domain.constant.DomainConstants.UTC;

public class Customer extends AggregateRoot<CustomerId> {
    private final String username;
    private final String firstName;
    private final String lastName;

    private ZonedDateTime createdAt;

    public Customer(Builder builder) {
        super.setId(builder.customerId);
        username = builder.username;
        firstName = builder.firstName;
        lastName = builder.lastName;
        createdAt = builder.createdAt;
    }

    public void initializeCustomer(){
        setId(new CustomerId(UUID.randomUUID()));
        createdAt = ZonedDateTime.now(ZoneId.of(UTC));
    }

    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void validateCustomer(Customer customer) {
        if (customer.getFirstName().equals(customer.getLastName())) {
            throw new CustomerDomainException("First name must be different then Last name");
        }
    }

    public static final class Builder {
        private CustomerId customerId;
        private String username;
        private String firstName;
        private String lastName;
        private ZonedDateTime createdAt;

        private Builder() {
        }

        public Builder customerId(CustomerId val) {
            customerId = val;
            return this;
        }

        public Builder username(String val) {
            username = val;
            return this;
        }

        public Builder firstName(String val) {
            firstName = val;
            return this;
        }

        public Builder lastName(String val) {
            lastName = val;
            return this;
        }

        public Builder createdAt(ZonedDateTime val) {
            createdAt = val;
            return this;
        }

        public Customer build() {
            return new Customer(this);
        }
    }
}
