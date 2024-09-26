package ok.backend.schedule.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ok.backend.schedule.domain.entity.Routine;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoutineResponseDto {
    private Long id;
    private Long scheduleId;
    private String repeatType;
    private Integer repeatInterval;
    private String[] repeatOn;

    public RoutineResponseDto(Routine routine) {
        this.id = routine.getId();
        this.scheduleId = routine.getSchedule().getId();
        this.repeatType = routine.getRepeatType().toString();
        this.repeatInterval = routine.getRepeatInterval();
        this.repeatOn = routine.getRepeatOn().stream().map(Enum::toString).toArray(String[]::new);
    }
}