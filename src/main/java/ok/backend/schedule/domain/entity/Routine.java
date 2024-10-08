package ok.backend.schedule.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ok.backend.schedule.domain.enums.DayOfWeek;
import ok.backend.schedule.domain.enums.RepeatType;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@Table(name = "routine")
public class Routine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 일정
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    // 반복 타입 (daily, weekly, monthly, yearly)
    @Enumerated(EnumType.STRING)
    @Column(name = "repeat_type", nullable = false)
    private RepeatType repeatType;

    // 반복 간격
    @Column(name = "repeat_interval", nullable = false)
    private Integer repeatInterval;

    // 주간 반복일 때 요일 선택 (여러 요일 선택 가능)
    @ElementCollection(targetClass = DayOfWeek.class)
    @CollectionTable(name = "day_of_week", joinColumns = @JoinColumn(name = "routine_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")
    private Set<DayOfWeek> dayOfWeek;

    // 월간 반복 일때 일자 선택(여러 일자 선택 가능)
    @ElementCollection
    @CollectionTable(name = "day_of_month", joinColumns = @JoinColumn(name = "routine_id"))
    @Column(name = "day_of_month")
    private Set<Integer> dayOfMonth;

    // 반복 일정의 종료일, null 이면 일정 계속 반복
    @Column(name = "end_date")
    private LocalDateTime endDate;
}