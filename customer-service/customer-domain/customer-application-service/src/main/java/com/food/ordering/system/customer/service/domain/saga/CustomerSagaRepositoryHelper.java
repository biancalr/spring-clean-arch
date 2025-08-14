package com.food.ordering.system.customer.service.domain.saga;

import com.food.ordering.system.domain.valueobject.CustomerStatus;
import com.food.ordering.system.saga.SagaStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomerSagaRepositoryHelper {

    public SagaStatus customerStatusToSagaStatus(CustomerStatus customerStatus) {
        if (CustomerStatus.CREATED == customerStatus) {
            return SagaStatus.SUCCEEDED;
        } else {
            return SagaStatus.FAILED;
        }
    }
}
