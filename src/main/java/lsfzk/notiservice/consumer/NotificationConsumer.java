package lsfzk.notiservice.consumer;

import lsfzk.notiservice.model.DeliveryNotification;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumer {

    @KafkaListener(topics = "delivery-updates", groupId = "notification-group")
    public void handleNotification(DeliveryNotification notification) {
        // Implement notification logic (SMS/email/push)
        System.out.println("Sending notification: " + notification);
    }
}
