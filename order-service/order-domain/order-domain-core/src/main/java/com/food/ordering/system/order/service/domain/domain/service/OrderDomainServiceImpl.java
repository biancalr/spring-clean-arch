package com.food.ordering.system.order.service.domain.domain.service;

import com.food.ordering.system.order.service.domain.domain.entity.Order;
import com.food.ordering.system.order.service.domain.domain.entity.Product;
import com.food.ordering.system.order.service.domain.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.domain.event.OrderCancelledEvent;
import com.food.ordering.system.order.service.domain.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.domain.event.OrderPaidEvent;
import com.food.ordering.system.order.service.domain.domain.exception.OrderDomainException;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class OrderDomainServiceImpl implements OrderDomainService {

    public static final String UTC = "UTC";

    @Override
    public OrderCreatedEvent validateAndInitiate(Order order, Restaurant restaurant) {
        validateRestaurant(restaurant);
        setOrderProductInformation(order, restaurant);
        order.validateOrder();
        order.initializeOrder();
        log.info("Order with id: {} is initiated", order.getId().getValue());
        return new OrderCreatedEvent(order, ZonedDateTime.now(ZoneId.of(UTC)));
    }

    @Override
    public OrderPaidEvent payOrder(Order order) {
        order.pay();
        log.info("Order with id {} is paid", order.getId().getValue());
        return new OrderPaidEvent(order, ZonedDateTime.now(ZoneId.of(UTC)));
    }

    @Override
    public void approveOrder(Order order) {
        order.approve();
        log.info("Order with restaurantId {} is approved", order.getId().getValue());
    }

    @Override
    public OrderCancelledEvent cancelOrderPayment(Order order, List<String> failureMessages) {
        order.initCancel(failureMessages);
        log.info("Order payment is cancelling for order restaurantId {}", order.getId().getValue());
        return new OrderCancelledEvent(order, ZonedDateTime.now(ZoneId.of(UTC)));
    }

    @Override
    public void cancelOrder(Order order, List<String> failureMessages) {
        order.cancel(failureMessages);
        log.info("Order with restaurantId {} is cancelled", order.getId().getValue());
    }

    private void validateRestaurant(Restaurant restaurant) {
        if (!restaurant.isActive()) {
            throw new OrderDomainException("Restaurant with id " + restaurant.getId().getValue() +
                    " is currently not active");
        }
    }

    private void setOrderProductInformation(Order order, Restaurant restaurant) {
        // Cria um mapa de produtos do restaurante por ID
        final Map<Product, Product> restaurantProductMap = restaurant.getProducts().stream()
                .collect(Collectors.toMap(p -> p, p -> p));

        // Atualiza os produtos do pedido usando o mapa
        order.getItems().forEach(orderItem -> {
            Product currentProduct = orderItem.getProduct();
            Product restaurantProduct = restaurantProductMap.get(currentProduct);
            if (restaurantProduct != null) {
                currentProduct.updateWithConfirmedNameAndPrice(
                        restaurantProduct.getName(),
                        restaurantProduct.getPrice());
            }
        });
    }

}
