package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.order.service.domain.domain.service.OrderDomainService;
import com.food.ordering.system.order.service.domain.domain.service.OrderDomainServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public OrderDomainService orderDomainService() {
        return new OrderDomainServiceImpl();
    }

}
