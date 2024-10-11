package ok.backend.notification.domain.repository;

import ok.backend.notification.domain.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByReceiverIdOrderByCreateDateDesc(Long receiverId);

    List<Notification> findAllByReceiverIdAndIsReadFalseOrderByCreateDateDesc(Long receiverId);
}
