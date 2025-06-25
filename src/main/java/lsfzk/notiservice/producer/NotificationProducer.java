package lsfzk.notiservice.producer;

import lsfzk.notiservice.model.DeliveryNotification;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationProducer {
    private final KafkaTemplate<String, DeliveryNotification> kafkaTemplate;

    public NotificationProducer(KafkaTemplate<String, DeliveryNotification> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendDeliveryNotification(String topic, DeliveryNotification notification) {
        kafkaTemplate.send(topic, notification);
    }
}
