package ok.backend.schedule.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ok.backend.schedule.domain.entity.SubSchedule;
import ok.backend.schedule.domain.enums.Status;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SubScheduleResponseDto {
    private Long id;
    private Long scheduleId;
    private String title;
    private String description;
    private Status status;

    public SubScheduleResponseDto(SubSchedule subSchedule) {
        this.id = subSchedule.getId();
        this.scheduleId = subSchedule.getSchedule().getId();
        this.title = subSchedule.getTitle();
        this.description = subSchedule.getDescription();
        this.status = subSchedule.getStatus();
    }
}
