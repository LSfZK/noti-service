package lsfzk.notiservice.consumer;

//import lsfzk.notiservice.event.BusinessRegistrationEvent;
import lsfzk.events.BusinessRegistrationEvent;
//import lsfzk.notiservice.event.OrderShippedEvent; // Hypothetical new event
import lsfzk.notiservice.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);
    private final NotificationService notificationService;

    // We inject the service that contains the actual business logic.
    public KafkaConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Listens for business registration events.
     * When a message is received, it immediately delegates the processing
     * to the NotificationService.
     */
    @KafkaListener(topics = "business-registrations", groupId = "notification-group")
    public void consumeBusinessRegistrationEvent(BusinessRegistrationEvent event) {
        log.info("Received BusinessRegistrationEvent {}", event);
        log.info("Consumed BusinessRegistrationEvent, delegating to NotificationService...");
        notificationService.processNewBusinessRegistration(event);
    }

    /**
     * An example of another listener for a different event.
     * It calls a different method on the NotificationService.
     */
//    @KafkaListener(topics = "orders-shipped", groupId = "notification-group")
//    public void consumeOrderShippedEvent(OrderShippedEvent event) {
//        log.info("Consumed OrderShippedEvent, delegating to NotificationService...");
//        notificationService.processOrderShippedNotification(event);
//    }
}