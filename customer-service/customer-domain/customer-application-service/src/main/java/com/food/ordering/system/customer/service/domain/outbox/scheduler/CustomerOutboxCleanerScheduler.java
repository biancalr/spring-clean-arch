package com.food.ordering.system.customer.service.domain.outbox.scheduler;

import com.food.ordering.system.customer.service.domain.outbox.model.CustomerOutboxMessage;
import com.food.ordering.system.outbox.OutboxScheduler;
import com.food.ordering.system.outbox.OutboxStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class CustomerOutboxCleanerScheduler implements OutboxScheduler {

    private final CustomerOutboxHelper customerOutboxHelper;

    public CustomerOutboxCleanerScheduler(CustomerOutboxHelper customerOutboxHelper) {
        this.customerOutboxHelper = customerOutboxHelper;
    }

    @Transactional
    @Scheduled(cron = "@midnight")
    @Override
    public void processOutboxMessage() {
        Optional<List<CustomerOutboxMessage>> outboxMessagesResponse =
                customerOutboxHelper.getOrderOutboxMessageByOutboxStatus(
                        OutboxStatus.COMPLETED);
        if (outboxMessagesResponse.isPresent() && !outboxMessagesResponse.get().isEmpty()) {
            List<CustomerOutboxMessage> outboxMessages = outboxMessagesResponse.get();
            log.info("Received {} CustomerOutboxMessage for clean-up!", outboxMessages.size());
            customerOutboxHelper.deleteOrderOutboxMessageByOutboxStatus(OutboxStatus.COMPLETED);
            log.info("Deleted {} CustomerOutboxMessage!", outboxMessages.size());
        }
    }
}
