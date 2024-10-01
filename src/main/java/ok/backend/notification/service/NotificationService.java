package ok.backend.notification.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import ok.backend.common.security.util.SecurityUserDetailService;
import ok.backend.notification.domain.entity.Notification;
import ok.backend.notification.domain.entity.NotificationPending;
import ok.backend.notification.domain.repository.NotificationRepository;
import ok.backend.notification.dto.NotificationResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    private final SecurityUserDetailService securityUserDetailService;

    @Transactional
    public void saveNotification(NotificationPending notificationPending) {
        Notification notification = Notification.builder()
                .receiver(notificationPending.getSchedule().getMember())
                .message(notificationPending.getSchedule().getTitle() + " 알림이에요")
                .isRead(false)
                .build();

        notificationRepository.save(notification);
    }

    public List<NotificationResponseDto> getAllNotifications() {
        Long memberId = securityUserDetailService.getLoggedInMember().getId();

        return notificationRepository.findAllByReceiverId(memberId)
                .stream()
                .map(NotificationResponseDto::new)
                .collect(Collectors.toList());
    }
}
