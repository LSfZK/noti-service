package lsfzk.notiservice.service;

import com.google.firebase.messaging.FirebaseMessaging;
import lsfzk.notiservice.event.BusinessRegistrationEvent;
//import lsfzk.notiservice.event.OrderShippedEvent; // A hypothetical new event
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class KafkaConsumerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerService.class);
    // ... your WebClient for fetching device tokens ...
    private final JavaMailSender mailSender;
    private final FirebaseMessaging fcm;
    private final WebClient.Builder webClientBuilder;

    /**
     * Listens to the "business-registrations" topic.
     * Spring's JsonDeserializer automatically converts the incoming JSON
     * into a BusinessRegistrationEvent object before calling this method.
     */
    @KafkaListener(topics = "business-registrations", groupId = "notification-group")
    public void consumeBusinessRegistrationEvent(BusinessRegistrationEvent event) {
        log.info("Consumed BusinessRegistrationEvent: {}", event);
        // ... your logic to notify admins ...
    }

    /**
     * An example of a second listener for a different event from a different topic.
     */
//    @KafkaListener(topics = "orders-shipped", groupId = "notification-group")
//    public void consumeOrderShippedEvent(OrderShippedEvent event) {
//        log.info("Consumed OrderShippedEvent: {}", event);
//        // ... your logic to notify a user that their order has shipped ...
//    }
}