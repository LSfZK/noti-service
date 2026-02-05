package lsfzk.notiservice.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lsfzk.events.BusinessRegistrationEvent;
import lsfzk.events.PromoteRequestEvent;
import lsfzk.events.PromoteResponseEvent;
import lsfzk.notiservice.model.NotificationEntity;
import lsfzk.notiservice.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;
    private final FirebaseMessaging fcm;
    private final WebClient.Builder webClientBuilder;
//    private static final Logger businessLogger = LoggerFactory.getLogger("userLogger");
    private static final Logger notiLogger = LoggerFactory.getLogger("notiLogger");
    private static final Logger userLogger = LoggerFactory.getLogger("userLogger");
    private final NotificationRepository notificationRepo;

    @KafkaListener(topics = "business-registrations", groupId = "notification-group")
    public void processNewBusinessRegistration(BusinessRegistrationEvent event) {
        NotificationEntity notification = new NotificationEntity(event);
        notificationRepo.save(notification);

        if(Boolean.FALSE.equals((hasData(getDeviceTokens(4L))).block())) {
            userLogger.info("User {} has no active tokens. Notification saved to DB only.", notification.getRecipientId());
            return; // Only DB save, no Push
        }
        // 1. Send push notification
        getDeviceTokens(4L)
                .subscribe(tokens -> {
                    if (tokens != null && !tokens.isEmpty()) {
                        notiLogger.info("Found tokens for user {}: {}", event.userId(), tokens);
                        // 2. Send the push notification using these tokens.
                        for (String token : tokens) {
                            try {
                                notiLogger.info("Sending push notification to token: " + token);
                                sendPushNotification(token,
                                        notification);
                                notiLogger.info("Push notification sent successfully to token: " + token);
                            } catch (RuntimeException e) {
                                notiLogger.info("Failed to send notification to token " + token + ": " + e.getMessage());
                            }
                        }
                    } else {
                        notiLogger.info("No device tokens found for user " + event.userId());
                    }
                });

        // for test
        List<String> emailList = List.of("jerrydevengineer@gmail.com");
        // 2. Send Email notification
        String result = sendMultipleEmails(
//                event.adminEmails(),
                emailList,
                "New Store Addition Request",
                String.format("User %s has requested to add a new store: %s\nStore ID: %s\n\nApprove at: https://admin.example.com",
                        event.userId(), event.businessName(), event.registrationId())
        );
    }

    @KafkaListener(topics = "promote-request", groupId = "notification-group")
    public void processPromoteRequest(PromoteRequestEvent event) {
        NotificationEntity notification = new NotificationEntity(event);
        notificationRepo.save(notification);

        if(Boolean.FALSE.equals((hasData(getDeviceTokens(4L))).block())) {
            userLogger.info("User {} has no active tokens. Notification saved to DB only.", notification.getRecipientId());
            return; // Only DB save, no Push
        }
        // Send push notification
        sendMultiplePush(List.of(4L), notification.getId(), notification);
    }

    @KafkaListener(topics = "promote-response", groupId = "notification-group")
    public void processPromoteResponse(PromoteResponseEvent event) {
        NotificationEntity notification = new NotificationEntity(event);
        notificationRepo.save(notification);

        if(Boolean.FALSE.equals((hasData(getDeviceTokens(4L))).block())) {
            userLogger.info("User {} has no active tokens. Notification saved to DB only.", notification.getRecipientId());
            return; // Only DB save, no Push
        }
        // Send push notification
        sendMultiplePush(List.of(notification.getRecipientId()), notification.getId(), notification);
    }

    @KafkaListener(topics = "business-reg-result", groupId = "notification-group")
    public void processNewBusinessRegResult(BusinessRegistrationEvent event) {
        NotificationEntity notification = new NotificationEntity(event);
        notificationRepo.save(notification);

        if(Boolean.FALSE.equals((hasData(getDeviceTokens(4L))).block())) {
            userLogger.info("User {} has no active tokens. Notification saved to DB only.", notification.getRecipientId());
            return; // Only DB save, no Push
        }
        // Send push notification
        sendMultiplePush(List.of(notification.getRecipientId()), notification.getId(), notification);
    }

    public void sendMultiplePush(List<Long> receiversId, Long eventId, NotificationEntity notification) {
        receiversId.forEach(receiverId -> {
            sendPush(receiverId, eventId, notification);
        });
    }

    public void sendPush(Long receiverId, Long eventId, NotificationEntity notification) {
        getDeviceTokens(receiverId)
                .subscribe(tokens -> {
                    if (tokens != null && !tokens.isEmpty()) {
                        notiLogger.info("Found tokens for user {}: {}", receiverId, tokens);
                        // 2. Send the push notification using these tokens.
                        for (String token : tokens) {
                            try {
                                notiLogger.info("Sending push notification to token: " + token + " eventId: " + eventId);
                                sendPushNotification(token,
                                        notification);
                                notiLogger.info("Push notification sent successfully to token: " + token);
                            } catch (RuntimeException e) {
                                notiLogger.info("Failed to send notification to token " + token + " eventId: " + eventId + ": " + e.getMessage());
                            }
                        }
                    } else {
                        notiLogger.info("No device tokens found for user " + receiverId);
                    }
                });
    }

    public String sendPushNotification(String deviceToken, NotificationEntity noti) {
        Notification notification = Notification.builder()
                .setTitle(noti.getTitle())
                .setBody(noti.getMessage())
                .build();

        Message message = Message.builder()
                .setToken(deviceToken)
                .setNotification(notification)
//                .putData("route", noti.getRoute()) // For clicking to navigate
                .putData("notificationId", noti.getId().toString()) // For tracking read status later
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

    public String sendToTopic(String topic, String title, String body) {
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        Message message = Message.builder()
                .setTopic(topic)
                .setNotification(notification)
                .build();

        try {
            return FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            throw new RuntimeException("Error sending topic notification", e);
        }
    }

//    private String formatMessage(RoleUpgradeEvent event) {
//        return String.format("User %s requested %s role",
//                event.userId(), event.requestedRole());
//    }
//
//    private String formatEmailContent(RoleUpgradeEvent event) {
//        return String.format("User ID: %s\nRequested Role: %s\n\nApprove at: https://admin.example.com",
//                event.userId(), event.requestedRole());
//    }

    /**
     * Makes a synchronous call to the user-service's internal endpoint.
     * @param userId The ID of the user whose tokens are needed.
     * @return A Mono containing a list of device token strings.
     */
    private Mono<List<String>> getDeviceTokens(Long userId) {
        // The "user-service" part of the URL is the service name registered in Eureka.
        return webClientBuilder.build().get()
                .uri("http://user-service/internal/users/{userId}/devices", userId)
                .retrieve()
                // Use ParameterizedTypeReference to provide detailed generic type information.
                .bodyToMono(new ParameterizedTypeReference<List<String>>() {});
    }

    public Mono<Boolean> hasData(Mono<List<String>> monoList) {
        return monoList
                .map(list -> !list.isEmpty()) // Check list content
                .defaultIfEmpty(false);       // If Mono itself was empty, return false
    }
}
