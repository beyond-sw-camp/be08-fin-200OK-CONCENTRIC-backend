package ok.backend.scedule.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
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

    @Column(name = "repeat_type", nullable = false)
    private String repeatType;

    @Column(name = "repeat_interval", nullable = false)
    private Integer repeatInterval;

    @Column(name = "repeat_on")
    private String repeatOn;
}
