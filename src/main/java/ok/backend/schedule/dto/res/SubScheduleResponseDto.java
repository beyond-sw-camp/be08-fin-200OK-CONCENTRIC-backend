package ok.backend.schedule.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ok.backend.schedule.domain.entity.SubSchedule;
import ok.backend.schedule.domain.enums.Status;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SubScheduleResponseDto {
    private Long id;
    private Long scheduleId;
    private String title;
    private String description;
    private Status status;
    private String startDate;
    private String endDate;

    public SubScheduleResponseDto(SubSchedule subSchedule) {
        this.id = subSchedule.getId();
        this.scheduleId = subSchedule.getSchedule().getId();
        this.title = subSchedule.getTitle();
        this.description = subSchedule.getDescription();
        this.status = subSchedule.getStatus();
        this.startDate = subSchedule.getStartDate().toString();
        this.endDate = subSchedule.getEndDate().toString();
    }
}
