package ok.backend.schedule.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ok.backend.schedule.domain.entity.Schedule;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleListResponseDto {
    private Long id;
    private Long userId;
    private String nickname;
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
    private String teamName;

    public ScheduleListResponseDto(Schedule schedule, String teamName) {
        this.id = schedule.getId();
        this.userId = schedule.getMember().getId();
        this.nickname = schedule.getMember().getNickname();
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
        this.teamName = teamName;
    }
}