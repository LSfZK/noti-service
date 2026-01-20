package lsfzk.notiservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lsfzk.events.BusinessRegistrationEvent;
import lsfzk.events.PromoteRequestEvent;
import lsfzk.events.PromoteResponseEvent;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter @Setter
@NoArgsConstructor
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long recipientId; // Who is this for? (e.g., Admin ID = 1)

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String message;

    // 💡 KEY FEATURE: Read Status
    @Column(nullable = false)
    private boolean isRead = false;

    private String route; // Optional: Deep link (e.g., "/orders/123")

    @CreationTimestamp
    private LocalDateTime createdAt;

    public NotificationEntity(Long recipientId, String title, String message) {
        this.recipientId = recipientId;
        this.title = title;
        this.message = message;
    }

    public NotificationEntity(Long recipientId, String title, String message, String route) {
        this.recipientId = recipientId;
        this.title = title;
        this.message = message;
        this.route = route;
    }

    public NotificationEntity(BusinessRegistrationEvent event) {
        this.recipientId = 4L;
        this.title = "Business Registration Request";
        this.message = String.format("User %s has requested to add a new store: %s",
                event.userId(), event.businessName());
    }

    public NotificationEntity(PromoteRequestEvent event) {
        this.recipientId = 4L;
        this.title = "User Promotion Request";
        this.message = String.format("User %s has requested to be promoted",
                event.userId());
    }

    public NotificationEntity(PromoteResponseEvent event) {
        this.recipientId = event.userId();
        this.title = "User Promotion Response";
        if(event.approved()) {
            this.message = String.format("User %s's promote request is approved",
                    event.userId());
        } else {
            this.message = String.format("User %s's promote request is denied",
                    event.userId());
        }

    }
}
