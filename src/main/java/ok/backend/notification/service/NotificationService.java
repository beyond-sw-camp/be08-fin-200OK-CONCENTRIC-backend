package ok.backend.notification.service;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import ok.backend.common.exception.CustomException;
import ok.backend.common.exception.ErrorCode;
import ok.backend.common.security.util.SecurityUserDetailService;
import ok.backend.member.domain.entity.Member;
import ok.backend.member.service.EmailService;
import ok.backend.notification.domain.entity.Notification;
import ok.backend.notification.domain.entity.NotificationPending;
import ok.backend.notification.domain.enums.NotificationType;
import ok.backend.notification.domain.repository.NotificationRepository;
import ok.backend.notification.dto.NotificationResponseDto;
import ok.backend.schedule.domain.entity.Schedule;
import ok.backend.schedule.domain.entity.SubSchedule;
import ok.backend.storage.service.AwsFileService;
import ok.backend.team.domain.entity.Team;
import ok.backend.team.service.TeamService;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    private final SecurityUserDetailService securityUserDetailService;

    private final TeamService teamService;

    private final EmailService emailService;

    private final AwsFileService awsFileService;

    public void saveNotificationFromPending(NotificationPending notificationPending) throws MalformedURLException {

        if(notificationPending.getNotificationType().equals(NotificationType.PRIVATE)) {
            Member member = notificationPending.getSchedule().getMember();
            String image = null;
            if(member.getImageUrl() != null) {
                image = awsFileService.getUrl(member.getImageUrl());
            }

            Notification notification = Notification.builder()
                    .receiver(member)
                    .message(notificationPending.getSchedule().getTitle() + " 일정이 곧 시작됩니다")
                    .notificationType(NotificationType.PRIVATE)
                    .image(image)
                    .isRead(false)
                    .build();

            notificationRepository.save(notification);

        }else if(notificationPending.getNotificationType().equals(NotificationType.BEFORE_START_SCHEDULE)) {

            Team team = teamService.findById(notificationPending.getSchedule().getTeamId());
            List<Member> members = teamService.getTeamMembersList(team.getId());

            String image = null;
            if(team.getImageUrl() != null) {
                image = awsFileService.getUrl(team.getImageUrl());
            }

            for (Member member : members) {
                Notification notification = Notification.builder()
                        .receiver(member)
                        .message(team.getName() + " 의 " + notificationPending.getSchedule().getTitle() + " 일정이 곧 시작됩니다")
                        .notificationType(NotificationType.BEFORE_START_SCHEDULE)
                        .image(image)
                        .isRead(false)
                        .build();

                notificationRepository.save(notification);
            }
        }
    }

    public void saveNotificationFromSchedule(Schedule schedule) throws MalformedURLException, MessagingException {

        Team team = teamService.findById(schedule.getTeamId());
        List<Member> members = teamService.getTeamMembersList(team.getId());

        String image = null;
        if(team.getImageUrl() != null) {
            image = awsFileService.getUrl(team.getImageUrl());
        }

        for (Member member : members) {
            Notification notification = Notification.builder()
                    .receiver(member)
                    .message(team.getName() + " 의 " + schedule.getTitle() + " 일정이 완료되었습니다")
                    .notificationType(NotificationType.SCHEDULE)
                    .image(image)
                    .isRead(false)
                    .build();

            notificationRepository.save(notification);

            emailService.sendNotificationEmailFromSchedule(schedule, member);
        }
    }

    public void saveNotificationFromSubSchedule(SubSchedule subSchedule) throws MalformedURLException, MessagingException {
        Member currentMember = securityUserDetailService.getLoggedInMember();

        Team team = teamService.findById(subSchedule.getSchedule().getTeamId());
        List<Member> members = teamService.getTeamMembersList(team.getId());

        String image = null;
        if(team.getImageUrl() != null) {
            image = awsFileService.getUrl(team.getImageUrl());
        }

        for (Member member : members) {
            Notification notification = Notification.builder()
                    .receiver(member)
                    .message(team.getName() + " : " + currentMember.getNickname() + "님이 " + subSchedule.getSchedule().getTitle() + " 일정의 " + subSchedule.getTitle() + "를 완료하였습니다.")
                    .notificationType(NotificationType.SCHEDULE)
                    .image(image)
                    .isRead(false)
                    .build();

            notificationRepository.save(notification);

            emailService.sendNotificationEmailFromSubSchedule(subSchedule, member);
        }
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
