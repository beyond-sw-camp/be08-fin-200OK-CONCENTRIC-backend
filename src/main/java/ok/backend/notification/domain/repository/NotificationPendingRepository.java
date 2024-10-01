package ok.backend.notification.domain.repository;

import ok.backend.notification.domain.entity.NotificationPending;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationPendingRepository extends JpaRepository<NotificationPending, Long> {
    List<NotificationPending> findAllBySendDateLessThanEqualAndIsSentFalseOrderBySendDateAsc(LocalDateTime date);
}
