package ok.backend.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import ok.backend.member.service.EmailService;
import ok.backend.notification.domain.entity.NotificationPending;
import ok.backend.notification.domain.enums.NotificationType;
import ok.backend.notification.domain.repository.NotificationPendingRepository;
import ok.backend.schedule.domain.entity.Schedule;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationPendingService {

    private final NotificationPendingRepository notificationPendingRepository;

    private final NotificationService notificationService;

    private final EmailService emailService;

    @Transactional
    public void saveScheduleToPending(Schedule schedule) {
        if(schedule.getStartNotification()){
            NotificationPending notificationPending = NotificationPending.builder()
                    .schedule(schedule)
                    .isSent(false)
                    .sendDate(schedule.getStartDate().minusHours(1L))
                    .notificationType(NotificationType.BEFORE_START_SCHEDULE)
                    .build();

            notificationPendingRepository.save(notificationPending);
        }

        if(schedule.getEndNotification()){
            NotificationPending notificationPending = NotificationPending.builder()
                    .schedule(schedule)
                    .isSent(false)
                    .sendDate(schedule.getEndDate().minusDays(1L))
                    .notificationType(NotificationType.BEFORE_END_SCHEDULE)
                    .build();

            notificationPendingRepository.save(notificationPending);
        }
    }

    @Scheduled(cron = "0 0/1 * * * *")
    @Transactional
    public void SendAndSaveNotification() throws MessagingException {
        List<NotificationPending> notificationPendingList =
                notificationPendingRepository.findAllBySendDateLessThanEqualAndIsSentFalseOrderBySendDateAsc(LocalDateTime.now());

        for(NotificationPending notificationPending : notificationPendingList){
            notificationService.saveNotification(notificationPending);

            emailService.sendNotificationEmail(notificationPending);

            notificationPending.updateStatus();
            notificationPendingRepository.save(notificationPending);
        }
    }
}
