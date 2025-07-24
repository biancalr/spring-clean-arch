package com.food.ordering.system.order.service.domain.port.output.repository;

import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.valueObject.TrackingId;

import java.util.Optional;

public interface OrderRepository {

    Order save(Order order);
    Optional<Order> findBytrackingId(TrackingId trackingId);
}
