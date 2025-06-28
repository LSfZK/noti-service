package lsfzk.notiservice.controller;

import lsfzk.notiservice.service.NotificationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/noti")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/send-to-device")
    public String sendToDevice(
            @RequestParam String deviceToken,
            @RequestParam String title,
            @RequestParam String body) {
        return notificationService.sendPushNotification(deviceToken, title, body);
    }

    @PostMapping("/send-to-topic")
    public String sendToTopic(
            @RequestParam String topic,
            @RequestParam String title,
            @RequestParam String body) {
        return notificationService.sendToTopic(topic, title, body);
    }
}
