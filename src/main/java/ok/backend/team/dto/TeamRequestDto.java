package ok.backend.team.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;



@Getter
@Setter
@Data
public class TeamRequestDto {
    // private Long id;

    // private Long chatroom_id;

    private String name;

    private Long creatorId;

    // private LocalDateTime createAt;
}
