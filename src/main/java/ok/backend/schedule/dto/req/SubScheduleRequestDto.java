package ok.backend.schedule.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ok.backend.schedule.domain.enums.Status;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubScheduleRequestDto {
    private String title;
    private String description;
    private Status status;
}
