package ok.backend.schedule.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleRequestDto {
    private Long userId;
    private String title;
    private String description;
    private String status;
    private String startDate;
    private String endDate;
    private Integer importance;
}