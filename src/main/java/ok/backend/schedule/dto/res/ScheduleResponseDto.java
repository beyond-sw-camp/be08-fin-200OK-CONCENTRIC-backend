package ok.backend.schedule.dto.res;

import ok.backend.schedule.domain.entity.Schedule;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponseDto {
    private Long id;
    private String title;
    private String description;
    private String status;
    private String startDate;
    private String endDate;

    public ScheduleResponseDto(Schedule schedule) {
        this.id = schedule.getId();
        this.title = schedule.getTitle();
        this.description = schedule.getDescription();
        this.status = schedule.getStatus();
        this.startDate = schedule.getStartDate().toString();
        this.endDate = schedule.getEndDate().toString();
    }
}
