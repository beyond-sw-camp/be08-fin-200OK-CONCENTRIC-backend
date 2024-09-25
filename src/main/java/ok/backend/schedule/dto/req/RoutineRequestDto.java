package ok.backend.schedule.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutineRequestDto {
    private Long scheduleId;
    private String repeatType;
    private Integer repeatInterval;
    private String[] repeatOn;
}