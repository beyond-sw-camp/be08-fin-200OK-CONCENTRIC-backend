package ok.backend.schedule.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ok.backend.schedule.domain.enums.Status;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubScheduleRequestDto {
    private Long scheduleId;
    private String title;
    private String description;
    private Status status;
    private String startDate;
    private String endDate;
}
