package ok.backend.schedule.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ok.backend.schedule.domain.enums.DayOfWeek;
import ok.backend.schedule.domain.enums.RepeatType;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutineRequestDto {
    private Long scheduleId;
    private RepeatType repeatType;
    private Integer repeatInterval;
    private DayOfWeek[] dayOfWeek;
    private Integer[] dayOfMonth;
    private String endDate;
}
