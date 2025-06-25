package lsfzk.notiservice.controller;

import lsfzk.notiservice.model.DeliveryNotification;
import lsfzk.notiservice.model.StatusUpdate;
import lsfzk.notiservice.producer.NotificationProducer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/deliveries")
public class DeliveryController {

    private final NotificationProducer notificationProducer;

    public DeliveryController(NotificationProducer notificationProducer) {
        this.notificationProducer = notificationProducer;
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<String> updateDeliveryStatus(
            @PathVariable String id,
            @RequestBody StatusUpdate update) {

        // Your delivery status update logic

        // Send notification
        DeliveryNotification notification = new DeliveryNotification(
                id,
                update.getOrderId(),
                update.getStatus(),
                LocalDateTime.now()
        );

        notificationProducer.sendDeliveryNotification("delivery-updates", notification);
        return ResponseEntity.ok("Status updated");
    }
}
