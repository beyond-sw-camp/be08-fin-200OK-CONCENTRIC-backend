package ok.backend.schedule.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import ok.backend.schedule.domain.enums.DayOfWeek;
import ok.backend.schedule.domain.enums.RepeatType;

import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Table(name = "routine")
public class Routine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "routine_id")
    private Long routineId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @Enumerated(EnumType.STRING)
    @Column(name = "repeat_type", nullable = false)
    private RepeatType repeatType;

    @Column(name = "repeat_interval", nullable = false)
    private Integer repeatInterval;

    @ElementCollection(targetClass = DayOfWeek.class)
    @CollectionTable(name = "routine_repeat_on", joinColumns = @JoinColumn(name = "routine_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "repeat_on")
    private Set<DayOfWeek> repeatOn;
}