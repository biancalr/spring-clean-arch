package com.food.ordering.system.order.service.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.ordering.system.domain.valueobject.*;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.service.domain.dto.create.OrderAddress;
import com.food.ordering.system.order.service.domain.dto.create.OrderItem;
import com.food.ordering.system.order.service.domain.entity.Customer;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentEventPayload;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentOutboxMessage;
import com.food.ordering.system.order.service.domain.port.input.service.OrderApplicationService;
import com.food.ordering.system.order.service.domain.port.output.repository.CustomerRepository;
import com.food.ordering.system.order.service.domain.port.output.repository.OrderRepository;
import com.food.ordering.system.order.service.domain.port.output.repository.PaymentOutboxRepository;
import com.food.ordering.system.order.service.domain.port.output.repository.RestaurantRepository;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.saga.SagaStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.food.ordering.system.saga.order.SagaConstants.ORDER_SAGA_NAME;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = OrderTestConfiguration.class)
public class OrderApplicationServiceTest {

    @Autowired
    private OrderApplicationService orderApplicationService;
    @Autowired
    private OrderDataMapper orderDataMapper;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private RestaurantRepository restaurantRepository;
    @Autowired
    private PaymentOutboxRepository paymentOutboxRepository;
    @Autowired
    private ObjectMapper objectMapper;

    private CreateOrderCommand createOrderCommand;
    private CreateOrderCommand createdOrderCommandWrongPrice;
    private CreateOrderCommand createOrderCommandWrongProductPrice;

    private final UUID CUSTOMER_ID = UUID.fromString("e9871389-18df-4768-a2fe-58872713c4b1");
    private final UUID RESTAURANT_ID = UUID.fromString("41984692-dced-41ca-8a1f-78f67912b898");
    private final UUID PRODUCT_ID_1 = UUID.fromString("8b893595-c228-4b5d-beae-7ce3f2db8fa5");
    private final UUID PRODUCT_ID_2 = UUID.fromString("2f68a54b-8e7a-43f9-b111-2866c0c7b3d0");
    private final UUID ORDER_ID = UUID.fromString("b47a7099-da07-45ac-a3d1-a4e15d01988f");
    private final UUID SAGA_ID = UUID.fromString("019863c5-3fe5-7b33-8d11-f5fbb1d386a4");
    private final BigDecimal PRICE = new BigDecimal("200.00");

    @BeforeAll
    public void init() {
        createOrderCommand = CreateOrderCommand.builder()
                .customerId(CUSTOMER_ID)
                .restaurantId(RESTAURANT_ID)
                .address(OrderAddress.builder()
                        .street("street_1")
                        .postalCode("1000AB")
                        .city("Paris")
                        .build())
                .price(PRICE)
                .items(List.of(OrderItem.builder()
                                .productId(PRODUCT_ID_1)
                                .quantity(1)
                                .price(new BigDecimal("50.00"))
                                .subTotal(new BigDecimal("50.00"))
                                .build(),
                        OrderItem.builder()
                                .productId(PRODUCT_ID_2)
                                .quantity(3)
                                .price(new BigDecimal("50.00"))
                                .subTotal(new BigDecimal("150.00"))
                                .build()))
                .build();

        createdOrderCommandWrongPrice = CreateOrderCommand.builder()
                .customerId(CUSTOMER_ID)
                .restaurantId(RESTAURANT_ID)
                .address(OrderAddress.builder()
                        .street("street_1")
                        .postalCode("1000AB")
                        .city("Paris")
                        .build())
                .price(new BigDecimal("250.00"))
                .items(List.of(OrderItem.builder()
                                .productId(PRODUCT_ID_1)
                                .quantity(1)
                                .price(new BigDecimal("50.00"))
                                .subTotal(new BigDecimal("50.00"))
                                .build(),
                        OrderItem.builder()
                                .productId(PRODUCT_ID_2)
                                .quantity(3)
                                .price(new BigDecimal("50.00"))
                                .subTotal(new BigDecimal("150.00"))
                                .build()))
                .build();
        createOrderCommandWrongProductPrice = CreateOrderCommand.builder()
                .customerId(CUSTOMER_ID)
                .restaurantId(RESTAURANT_ID)
                .address(OrderAddress.builder()
                        .street("street_1")
                        .postalCode("1000AB")
                        .city("Paris")
                        .build())
                .price(PRICE)
                .items(List.of(OrderItem.builder()
                                .productId(PRODUCT_ID_1)
                                .quantity(1)
                                .price(new BigDecimal("60.00"))
                                .subTotal(new BigDecimal("50.00"))
                                .build(),
                        OrderItem.builder()
                                .productId(PRODUCT_ID_2)
                                .quantity(3)
                                .price(new BigDecimal("50.00"))
                                .subTotal(new BigDecimal("150.00"))
                                .build()))
                .build();

        Customer customer = new Customer();
        customer.setId(new CustomerId(CUSTOMER_ID));

        Restaurant restaurantResponse = Restaurant.builder()
                .restaurantId(new RestaurantId(RESTAURANT_ID))
                .products(List.of(new Product(new ProductId(PRODUCT_ID_1), "product-1", new Money(new BigDecimal("50.00"))),
                        new Product(new ProductId(PRODUCT_ID_2), "product-2", new Money(new BigDecimal("50.00")))))
                .active(true)
                .build();

        Order order = orderDataMapper.createOrderCommandToOrder(createOrderCommand);
        order.setId(new OrderId(ORDER_ID));

        when(customerRepository.findCustomer(CUSTOMER_ID)).thenReturn(Optional.of(customer));
        when(restaurantRepository.findRestaurantinformation(orderDataMapper.createOrderCommandToRestaurant(createOrderCommand)))
                .thenReturn(Optional.of(restaurantResponse));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(paymentOutboxRepository.save(any(OrderPaymentOutboxMessage.class)))
                .thenReturn(getOrderPaymentOutboxMessage());
    }

    @Test
    public void testCreateOrder() {
        CreateOrderResponse createOrderResponse = orderApplicationService.createOrder(createOrderCommand);
        assertEquals(OrderStatus.PENDING, createOrderResponse.getOrderStatus());
        assertEquals("Order created successfully", createOrderResponse.getMessage());
        assertNotNull(createOrderResponse.getOrderTrackingId());
    }

    @Test
    public void testWrongTotalPrice() {
        OrderDomainException orderDomainException = assertThrows(OrderDomainException.class, () -> orderApplicationService.createOrder(createdOrderCommandWrongPrice));
        assertEquals("Total price: 250.00 is not equal to order items total: 200.00", orderDomainException.getMessage());

    }

    @Test
    public void testWrongProductPrice() {
        OrderDomainException orderDomainException = assertThrows(OrderDomainException.class, () -> orderApplicationService.createOrder(createOrderCommandWrongProductPrice));
        assertEquals("Order item price: 60.00 is not valid for product: " + PRODUCT_ID_1, orderDomainException.getMessage());

    }

    @Test
    public void testCreateOrderWithPassiveRestaurant() {
        final Restaurant restaurantResponse = Restaurant.builder()
                .restaurantId(new RestaurantId(RESTAURANT_ID))
                .products(List.of(new Product(new ProductId(PRODUCT_ID_1), "product-1", new Money(new BigDecimal("50.00"))),
                        new Product(new ProductId(PRODUCT_ID_2), "product-2", new Money(new BigDecimal("50.00")))))
                .active(false)
                .build();
        when(restaurantRepository.findRestaurantinformation(orderDataMapper.createOrderCommandToRestaurant(createOrderCommand)))
                .thenReturn(Optional.of(restaurantResponse));
        OrderDomainException orderDomainException =
                assertThrows(OrderDomainException.class, () -> orderApplicationService.createOrder(createOrderCommand));
        assertEquals("Restaurant with id " + RESTAURANT_ID + " is currently not active", orderDomainException.getMessage());
    }

    private OrderPaymentOutboxMessage getOrderPaymentOutboxMessage() {
        OrderPaymentEventPayload orderPaymentEventPayload = OrderPaymentEventPayload.builder()
                .orderId(ORDER_ID.toString())
                .customerId(CUSTOMER_ID.toString())
                .price(PRICE)
                .createdAt(ZonedDateTime.now())
                .paymentOrderStatus(PaymentOrderStatus.PENDING.name())
                .build();

        return OrderPaymentOutboxMessage.builder()
                .id(UUID.randomUUID())
                .sagaId(SAGA_ID)
                .createdAt(ZonedDateTime.now())
                .type(ORDER_SAGA_NAME)
                .payload(createPayload(orderPaymentEventPayload))
                .orderStatus(OrderStatus.PENDING)
                .sagaStatus(SagaStatus.STARTED)
                .outboxStatus(OutboxStatus.STARTED)
                .version(0)
                .build();
    }

    private String createPayload(OrderPaymentEventPayload orderPaymentEventPayload) {
        try {
            return objectMapper
                    .writeValueAsString(orderPaymentEventPayload);
        } catch (JsonProcessingException e) {
            throw new OrderDomainException("Cannot create OrderPaymentEventPayload object!");
        }
    }
}
