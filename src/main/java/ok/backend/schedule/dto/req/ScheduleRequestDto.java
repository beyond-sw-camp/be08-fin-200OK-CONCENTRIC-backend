package ok.backend.schedule.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ok.backend.schedule.domain.enums.Status;
import ok.backend.schedule.domain.enums.Type;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleRequestDto {
    private String title;
    private String description;
    private Status status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer importance;
    private Type type;
    private Long teamId;
}