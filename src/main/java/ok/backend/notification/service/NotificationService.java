package ok.backend.notification.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import ok.backend.common.exception.CustomException;
import ok.backend.common.exception.ErrorCode;
import ok.backend.common.security.util.SecurityUserDetailService;
import ok.backend.member.domain.entity.Member;
import ok.backend.notification.domain.entity.Notification;
import ok.backend.notification.domain.entity.NotificationPending;
import ok.backend.notification.domain.repository.NotificationRepository;
import ok.backend.notification.dto.NotificationResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    private final SecurityUserDetailService securityUserDetailService;

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

        return notificationRepository.findAllByReceiverIdOrderByCreateDateDesc(memberId)
                .stream()
                .map(NotificationResponseDto::new)
                .collect(Collectors.toList());
    }

    public List<NotificationResponseDto> getNotReadNotifications() {
        Long memberId = securityUserDetailService.getLoggedInMember().getId();

        return notificationRepository.findAllByReceiverIdAndIsReadFalseOrderByCreateDateDesc(memberId)
                .stream()
                .map(NotificationResponseDto::new)
                .collect(Collectors.toList());
    }

    public NotificationResponseDto updateNotificationReadById(Long notificationId){
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(() ->
                new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));

        notification.updateRead();
        return new NotificationResponseDto(notificationRepository.save(notification));
    }

    public void deleteNotifications() {
        Long memberId = securityUserDetailService.getLoggedInMember().getId();

        List<Notification> notifications = notificationRepository.findAllByReceiverIdAndIsReadTrue(memberId);

        notificationRepository.deleteAll(notifications);
    }
}
