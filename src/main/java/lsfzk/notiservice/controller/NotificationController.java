package lsfzk.notiservice.controller;

import lombok.RequiredArgsConstructor;
import lsfzk.notiservice.model.NotificationEntity;
import lsfzk.notiservice.repository.NotificationRepository;
import lsfzk.notiservice.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/noti")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationRepository notificationRepo;

    // 1. Get My Notifications (History)
    @GetMapping("/me")
    public ResponseEntity<List<NotificationEntity>> getMyNotifications(
            @RequestHeader("X-User-Id") Long userId) { // Assuming ID comes from Gateway/Auth

        // Fetch most recent first
        List<NotificationEntity> list = notificationRepo.findByRecipientIdOrderByCreatedAtDesc(userId);
        return ResponseEntity.ok(list);
    }

    // 2. Get Unread Count (For the Red Badge 🔴)
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(@RequestHeader("X-User-Id") Long userId) {
        long count = notificationRepo.countByRecipientIdAndIsReadFalse(userId);
        return ResponseEntity.ok(count);
    }

    // 3. Mark as Read (When user clicks the bell or a specific item)
    @PatchMapping("/{id}/read")
    @Transactional
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationRepo.findById(id).ifPresent(n -> {
            n.setRead(true);
            notificationRepo.save(n);
        });
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send-to-device")
    public String sendToDevice(
            @RequestParam String deviceToken,
            @RequestParam NotificationEntity notificationEntity) {
        return notificationService.sendPushNotification(deviceToken, notificationEntity);
    }

    @PostMapping("/send-to-topic")
    public String sendToTopic(
            @RequestParam String topic,
            @RequestParam String title,
            @RequestParam String body) {
        return notificationService.sendToTopic(topic, title, body);
    }
}
