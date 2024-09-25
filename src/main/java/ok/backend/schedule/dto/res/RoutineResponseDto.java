package ok.backend.schedule.dto.res;

import ok.backend.schedule.domain.entity.Routine;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoutineResponseDto {
    private Long id;
    private String repeatType;
    private Integer repeatInterval;

    public RoutineResponseDto(Routine routine) {
        this.id = routine.getId();
        this.repeatType = routine.getRepeatType().toString();
        this.repeatInterval = routine.getRepeatInterval();
    }
}