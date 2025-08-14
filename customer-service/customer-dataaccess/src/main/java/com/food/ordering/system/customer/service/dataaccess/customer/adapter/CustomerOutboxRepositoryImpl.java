package com.food.ordering.system.customer.service.dataaccess.customer.adapter;

import com.food.ordering.system.customer.service.dataaccess.customer.exception.CustomerOutboxNotFoundException;
import com.food.ordering.system.customer.service.dataaccess.customer.mapper.CustomerDataAccessMapper;
import com.food.ordering.system.customer.service.dataaccess.customer.repository.CustomerOutboxJpaRepository;
import com.food.ordering.system.customer.service.domain.outbox.model.CustomerOutboxMessage;
import com.food.ordering.system.customer.service.domain.ports.output.repository.CustomerOutboxRepository;
import com.food.ordering.system.outbox.OutboxStatus;

import java.util.*;

public class CustomerOutboxRepositoryImpl implements CustomerOutboxRepository {

    private final CustomerOutboxJpaRepository customerOutboxJpaRepository;
    private final CustomerDataAccessMapper customerDataAccessMapper;

    public CustomerOutboxRepositoryImpl(CustomerOutboxJpaRepository customerOutboxJpaRepository, CustomerDataAccessMapper customerDataAccessMapper) {
        this.customerOutboxJpaRepository = customerOutboxJpaRepository;
        this.customerDataAccessMapper = customerDataAccessMapper;
    }

    @Override
    public CustomerOutboxMessage save(CustomerOutboxMessage customerOutboxMessage) {
        return
                customerDataAccessMapper.customerOutboxEntityToOrderOutboxMessage(customerOutboxJpaRepository.save(customerDataAccessMapper
                        .orderOutboxMessageToCustomerOutboxEntity(customerOutboxMessage)));
    }

    @Override
    public Optional<List<CustomerOutboxMessage>> findByTypeAndOutboxStatus(
            String sagaType,
            OutboxStatus outboxStatus) {
        return Optional.of(customerOutboxJpaRepository.findByTypeAndOutboxStatus(sagaType, outboxStatus)
                .orElseThrow(() -> new CustomerOutboxNotFoundException("Customer outbox object " +
                        "could not be found for type " + sagaType + " outbox status " + outboxStatus))
                .stream()
                .map(customerDataAccessMapper::customerOutboxEntityToOrderOutboxMessage)
                .toList());
    }

    @Override
    public Optional<CustomerOutboxMessage> findByTypeAndSagaIdAndOutboxStatus(
            String sagaType,
            UUID sagaId,
            OutboxStatus outboxStatus) {
        return customerOutboxJpaRepository.findByTypeAndSagaIdAndOutboxStatus(sagaType, sagaId, outboxStatus)
                .map(customerDataAccessMapper::customerOutboxEntityToOrderOutboxMessage);
    }

    @Override
    public void deleteByTypeAndOutboxStatus(String type, OutboxStatus... outboxStatus) {
        customerOutboxJpaRepository.deleteByTypeAndOutboxStatusIn(type,
                Collections.unmodifiableList(Arrays.asList(outboxStatus)));
    }
}
