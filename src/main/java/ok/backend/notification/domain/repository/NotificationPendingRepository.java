package ok.backend.notification.domain.repository;

import ok.backend.notification.domain.entity.NotificationPending;
import ok.backend.notification.domain.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationPendingRepository extends JpaRepository<NotificationPending, Long> {
    List<NotificationPending> findAllBySendDateLessThanEqualAndIsSentFalseOrderBySendDateAsc(LocalDateTime date);

    Optional<NotificationPending> findByScheduleIdAndNotificationTypeAndIsSentFalse(Long scheduleId, NotificationType notificationType);
}
