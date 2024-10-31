package ok.backend.schedule.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ok.backend.schedule.domain.entity.Schedule;
import ok.backend.schedule.domain.enums.Status;
import ok.backend.schedule.domain.enums.Type;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponseDto {
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
    private Integer importance;
    private Integer progress;
    private String type;
    private Long teamId;

    public ScheduleResponseDto(Schedule schedule) {
        this.id = schedule.getId();
        this.userId = schedule.getMember().getId();
        this.title = schedule.getTitle();
        this.description = schedule.getDescription();
        this.status = schedule.getStatus().toString();
        this.startDate = schedule.getStartDate();
        this.endDate = schedule.getEndDate();
        this.importance = schedule.getImportance();
        this.createAt = schedule.getCreateAt();
        this.updateAt = schedule.getUpdateAt();
        this.progress = schedule.getProgress();
        this.type = schedule.getType().toString();
        this.teamId = schedule.getTeamId();
    }
}