package ok.backend.notification.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ok.backend.member.domain.entity.Member;
import ok.backend.notification.domain.enums.NotificationType;
import org.checkerframework.checker.units.qual.C;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Table(name = "notification")
public class Notification {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private Member receiver;

    @Column(nullable = false)
    private String message;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createDate;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead;

    @Column(name = "type", nullable = false)
    private NotificationType notificationType;

    @Column(name = "image")
    private String image;

    public void updateRead(){
        this.isRead = !this.isRead;
    }
}
