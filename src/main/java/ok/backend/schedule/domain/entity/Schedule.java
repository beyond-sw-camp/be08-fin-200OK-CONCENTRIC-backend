package ok.backend.schedule.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ok.backend.member.domain.entity.Member;
import ok.backend.schedule.domain.enums.Status;
import ok.backend.schedule.domain.enums.Type;
import ok.backend.schedule.dto.req.ScheduleRequestDto;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.List;

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

    // 회원 번호
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Member member;

    // 제목
    @Column(nullable = false)
    private String title;

    // 설명
    @Column
    private String description;

    // 상태 ( ACTIVE(진행 중), INACTIVE(중지), COMPLETED(완료) )
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    // 시작일
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    // 종료일
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    // 생성일
    @CreationTimestamp
    @Column(name = "create_at", nullable = false, updatable = false)
    private LocalDateTime createAt;

    // 수정일
    @UpdateTimestamp
    @Column(name = "update_at", nullable = false)
    private LocalDateTime updateAt;

    // 중요도 (0~5)
    @Column(name = "importance")
    private Integer importance;

    // 반복 일정 (1:1)
//    @OneToOne(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
//    private Routine routine;

    // 팀 일정 (1:1)
//    @OneToOne(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
//    private TeamSchedule teamSchedule;

    // 하위 일정 (1:many)
    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubSchedule> subSchedules;  // 하나의 스케줄이 여러 하위 일정을 가짐

    // 일정 진행도 (0-100)
    @Column(name = "progress")
    private Integer progress;

    // 구분(팀, 개인)
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private Type type;

    // 팀ID
    @Column(name = "team_id")
    private Long teamId;


    public void updateFields(ScheduleRequestDto scheduleRequestDto) {
        this.title = scheduleRequestDto.getTitle();
        this.description = scheduleRequestDto.getDescription();
        this.status = scheduleRequestDto.getStatus();
        this.startDate = scheduleRequestDto.getStartDate();
        this.endDate = scheduleRequestDto.getEndDate();
        this.importance = scheduleRequestDto.getImportance();
        this.type = scheduleRequestDto.getType();
        this.teamId = scheduleRequestDto.getTeamId();
    }

    public void updateStatus(Status status) {
        this.status = status;
    }

    public void updateProgress(Integer progress) {
        this.progress = progress;
    }
}