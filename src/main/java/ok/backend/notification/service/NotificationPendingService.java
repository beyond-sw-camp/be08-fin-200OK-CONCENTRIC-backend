package ok.backend.notification.service;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ok.backend.member.service.EmailService;
import ok.backend.notification.domain.entity.NotificationPending;
import ok.backend.notification.domain.enums.NotificationType;
import ok.backend.notification.domain.repository.NotificationPendingRepository;
import ok.backend.schedule.domain.entity.Schedule;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NotificationPendingService {

    private final NotificationPendingRepository notificationPendingRepository;

    private final NotificationService notificationService;

    private final EmailService emailService;

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

    public void updateScheduleToPending(Schedule existingSchedule, Schedule updatedSchedule) {

        // check start notification
        if(!existingSchedule.getStartNotification() && updatedSchedule.getStartNotification()){
            NotificationPending notificationPending = NotificationPending.builder()
                    .schedule(updatedSchedule)
                    .isSent(false)
                    .sendDate(updatedSchedule.getStartDate().minusHours(1L))
                    .notificationType(NotificationType.BEFORE_START_SCHEDULE)
                    .build();

            notificationPendingRepository.save(notificationPending);

        }else if(existingSchedule.getStartNotification() && !updatedSchedule.getStartNotification()){
            Optional<NotificationPending> foundNotificationPending = notificationPendingRepository
                    .findByScheduleIdAndNotificationTypeAndIsSentFalse(existingSchedule.getId(), NotificationType.BEFORE_START_SCHEDULE);

            foundNotificationPending.ifPresent(notificationPendingRepository::delete);

        }else if(!existingSchedule.getStartDate().isEqual(updatedSchedule.getStartDate()) && updatedSchedule.getStartNotification()){
            Optional<NotificationPending> foundNotificationPending = notificationPendingRepository
                    .findByScheduleIdAndNotificationTypeAndIsSentFalse(existingSchedule.getId(), NotificationType.BEFORE_START_SCHEDULE);

            foundNotificationPending.ifPresent(notificationPendingRepository::delete);

            NotificationPending notificationPending = NotificationPending.builder()
                    .schedule(updatedSchedule)
                    .isSent(false)
                    .sendDate(updatedSchedule.getStartDate().minusHours(1L))
                    .notificationType(NotificationType.BEFORE_START_SCHEDULE)
                    .build();

            notificationPendingRepository.save(notificationPending);
        }

        // check end notification
        if(!existingSchedule.getEndNotification() && updatedSchedule.getEndNotification()){
            NotificationPending notificationPending = NotificationPending.builder()
                    .schedule(updatedSchedule)
                    .isSent(false)
                    .sendDate(updatedSchedule.getEndDate().minusDays(1L))
                    .notificationType(NotificationType.BEFORE_END_SCHEDULE)
                    .build();

            notificationPendingRepository.save(notificationPending);

        }else if(existingSchedule.getEndNotification() && !updatedSchedule.getEndNotification()){
            Optional<NotificationPending> foundNotificationPending = notificationPendingRepository
                    .findByScheduleIdAndNotificationTypeAndIsSentFalse(existingSchedule.getId(), NotificationType.BEFORE_END_SCHEDULE);

            foundNotificationPending.ifPresent(notificationPendingRepository::delete);

        }else if(!existingSchedule.getEndDate().isEqual(updatedSchedule.getEndDate()) && updatedSchedule.getEndNotification()){
            Optional<NotificationPending> foundNotificationPending = notificationPendingRepository
                    .findByScheduleIdAndNotificationTypeAndIsSentFalse(existingSchedule.getId(), NotificationType.BEFORE_END_SCHEDULE);

            foundNotificationPending.ifPresent(notificationPendingRepository::delete);

            NotificationPending notificationPending = NotificationPending.builder()
                    .schedule(updatedSchedule)
                    .isSent(false)
                    .sendDate(updatedSchedule.getEndDate().minusDays(1L))
                    .notificationType(NotificationType.BEFORE_END_SCHEDULE)
                    .build();

            notificationPendingRepository.save(notificationPending);
        }

    }

    @Scheduled(cron = "0 0/1 * * * *")
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
