package lsfzk.notiservice.model;

import java.time.LocalDateTime;

public record DeliveryNotification(
        String orderId,
        String customerId,
        String status,
        LocalDateTime timestamp
) {}
