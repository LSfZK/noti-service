package lsfzk.notiservice.repository;

import lsfzk.notiservice.model.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    long countByRecipientIdAndIsReadFalse(Long userId);
    List<NotificationEntity> findByRecipientIdOrderByCreatedAtDesc(Long userId);
}
