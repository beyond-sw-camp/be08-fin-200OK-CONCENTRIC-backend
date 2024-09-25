package ok.backend.schedule.dto.res;

import ok.backend.schedule.domain.entity.TeamSchedule;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TeamScheduleResponseDto {
    private Long id;
    private Long teamId;
    private Long scheduleId;

    public TeamScheduleResponseDto(TeamSchedule teamSchedule) {
        this.id = teamSchedule.getId();
        this.teamId = teamSchedule.getTeam().getId();
        this.scheduleId = teamSchedule.getSchedule().getId();
    }
}
