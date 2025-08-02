package com.food.ordering.system.customer.service;

import com.food.ordering.system.customer.service.domain.service.CustomerDomainService;
import com.food.ordering.system.customer.service.domain.service.CustomerDomainServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public CustomerDomainService customerDomainService() {
        return new CustomerDomainServiceImpl();
    }
}
