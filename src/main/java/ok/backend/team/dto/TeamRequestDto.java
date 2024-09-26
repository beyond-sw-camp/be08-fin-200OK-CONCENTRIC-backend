package ok.backend.team.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TeamRequestDto {
    private Long id;

    private Long chatroom_id;

    private String name;

    private Long creator_id;

    private LocalDateTime createAt;
}
