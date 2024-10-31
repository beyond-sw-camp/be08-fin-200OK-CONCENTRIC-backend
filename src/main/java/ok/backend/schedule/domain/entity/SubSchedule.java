package ok.backend.schedule.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ok.backend.schedule.domain.enums.Status;
import ok.backend.schedule.dto.req.SubScheduleRequestDto;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@Table(name = "sub_schedules")
public class SubSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 일정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    // 제목
    @Column(nullable = false)
    private String title;

    // 설명
    @Column
    private String description;

    // 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    public void updateFields(SubScheduleRequestDto subScheduleRequestDto) {
        this.title = subScheduleRequestDto.getTitle();
        this.description = subScheduleRequestDto.getDescription();
        this.status = subScheduleRequestDto.getStatus();
    }

    public void updateStatus(Status status) {
        this.status = status;
    }
}
