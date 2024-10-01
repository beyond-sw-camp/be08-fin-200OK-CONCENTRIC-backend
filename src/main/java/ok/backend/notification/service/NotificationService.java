package ok.backend.notification.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import ok.backend.notification.domain.entity.Notification;
import ok.backend.notification.domain.entity.NotificationPending;
import ok.backend.notification.domain.repository.NotificationRepository;
import ok.backend.schedule.domain.entity.Schedule;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void saveNotification(NotificationPending notificationPending) {
        Notification notification = Notification.builder()
                .receiver(notificationPending.getSchedule().getMember())
                .message(notificationPending.getSchedule().getTitle() + " 알림이에요")
                .isRead(false)
                .build();

        notificationRepository.save(notification);
    }
}
