package lsfzk.notiservice.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lsfzk.notiservice.model.StoreAddEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;
    private final FirebaseMessaging fcm;

    @KafkaListener(topics = "store-add-events")
    public void handleStoreAdd(StoreAddEvent event) {
        // 1. Send push notification
        sendPushNotification(event.deviceToken(),
                "Store Add Request",
                String.format("User %s has requested to add a new store: %s",
                        event.userId(), event.storeName()));
//                formatMessage(event));

        // 2. Send Email notification
        String result = sendMultipleEmails(event.adminEmails(),
                "New Store Addition Request",
                String.format("User %s has requested to add a new store: %s\nStore ID: %s\n\nApprove at: https://admin.example.com",
                        event.userId(), event.storeName(), event.storeId()));
    }

    public String sendPushNotification(String deviceToken, String title, String body) {
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        Message message = Message.builder()
                .setToken(deviceToken)
                .setNotification(notification)
                .build();

        try {
            return FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            throw new RuntimeException("Error sending notification", e);
        }
    }

    public String sendEmail(String to, String subject, String body) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(to);
        email.setSubject(subject);
        email.setText(body);
        mailSender.send(email);
        return "Email sent to " + to + " with subject: " + subject;
    }

    public String sendMultipleEmails(List<String> recipients, String subject, String body) {
        for(String recipient : recipients) {
            sendEmail(recipient, subject, body);
        }
        return "Emails sent to " + recipients.size() + " recipients with subject: " + subject;
    }

//    public String sendToTopic(String topic, String title, String body) {
//        Notification notification = Notification.builder()
//                .setTitle(title)
//                .setBody(body)
//                .build();
//
//        Message message = Message.builder()
//                .setTopic(topic)
//                .setNotification(notification)
//                .build();
//
//        try {
//            return FirebaseMessaging.getInstance().send(message);
//        } catch (FirebaseMessagingException e) {
//            throw new RuntimeException("Error sending topic notification", e);
//        }
//    }

//    private String formatMessage(RoleUpgradeEvent event) {
//        return String.format("User %s requested %s role",
//                event.userId(), event.requestedRole());
//    }
//
//    private String formatEmailContent(RoleUpgradeEvent event) {
//        return String.format("User ID: %s\nRequested Role: %s\n\nApprove at: https://admin.example.com",
//                event.userId(), event.requestedRole());
//    }
}
