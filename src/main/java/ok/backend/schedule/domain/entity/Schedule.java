package ok.backend.schedule.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import ok.backend.member.domain.entity.Member;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@Table(name = "schedules")
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private String title;

    @Column
    private String description;

    @Column(nullable = false)
    private String status;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @CreationTimestamp
    @Column(name = "create_at", nullable = false, updatable = false)
    private LocalDateTime createAt;

    @UpdateTimestamp
    @Column(name = "update_at", nullable = false)
    private LocalDateTime updateAt;

    @Column
    private Integer importance;

    @OneToOne(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    private Routine routine;

    @OneToOne(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    private TeamSchedule teamSchedule;

    // 필드 업데이트 메서드
    public void updateFields(Schedule updatedSchedule) {
        this.title = updatedSchedule.getTitle();
        this.description = updatedSchedule.getDescription();
        this.status = updatedSchedule.getStatus();
        this.startDate = updatedSchedule.getStartDate();
        this.endDate = updatedSchedule.getEndDate();
        this.importance = updatedSchedule.getImportance();
    }
}