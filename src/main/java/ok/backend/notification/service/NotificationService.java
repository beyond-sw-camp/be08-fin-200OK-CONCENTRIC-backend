package ok.backend.notification.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import ok.backend.common.exception.CustomException;
import ok.backend.common.exception.ErrorCode;
import ok.backend.common.security.util.SecurityUserDetailService;
import ok.backend.member.domain.entity.Member;
import ok.backend.notification.domain.entity.Notification;
import ok.backend.notification.domain.entity.NotificationPending;
import ok.backend.notification.domain.enums.NotificationType;
import ok.backend.notification.domain.repository.NotificationRepository;
import ok.backend.notification.dto.NotificationResponseDto;
import ok.backend.schedule.domain.entity.Schedule;
import ok.backend.team.domain.entity.Team;
import ok.backend.team.service.TeamService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    private final SecurityUserDetailService securityUserDetailService;

    private final TeamService teamService;

    public void saveNotificationFromPending(NotificationPending notificationPending) {

        if(notificationPending.getNotificationType().equals(NotificationType.PRIVATE)) {
            Notification notification = Notification.builder()
                    .receiver(notificationPending.getSchedule().getMember())
                    .message(notificationPending.getSchedule().getTitle() + " 일정이 곧 시작됩니다")
                    .notificationType(NotificationType.PRIVATE)
                    .image(notificationPending.getSchedule().getMember().getImageUrl())
                    .isRead(false)
                    .build();

            notificationRepository.save(notification);

        }else if(notificationPending.getNotificationType().equals(NotificationType.BEFORE_START_SCHEDULE)){

            Team team = teamService.findById(notificationPending.getSchedule().getTeamId());

            Notification notification = Notification.builder()
                    .receiver(notificationPending.getSchedule().getMember())
                    .message(team.getName() + " 의 " + notificationPending.getSchedule().getTitle() + " 일정이 곧 시작됩니다")
                    .notificationType(NotificationType.BEFORE_START_SCHEDULE)
                    .image(notificationPending.getSchedule().getMember().getImageUrl())
                    .isRead(false)
                    .build();

            notificationRepository.save(notification);

        }
    }

    public void saveNotification() {

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
