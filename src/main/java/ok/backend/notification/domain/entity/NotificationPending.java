package ok.backend.notification.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ok.backend.notification.domain.enums.NotificationType;
import ok.backend.schedule.domain.entity.Schedule;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Table(name = "notification_pending")
public class NotificationPending {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Schedule schedule;

    @Column(name = "is_sent", nullable = false)
    private Boolean isSent;

    @Column(name = "send_date", nullable = false)
    private LocalDateTime sendDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType notificationType;

    public void updateStatus(){
        this.isSent = true;
    }

    public void updateSchedule(Schedule schedule){
        this.schedule = schedule;
    }

    public void updateSendDate(LocalDateTime sendDate){
        this.sendDate = sendDate;
    }
}
