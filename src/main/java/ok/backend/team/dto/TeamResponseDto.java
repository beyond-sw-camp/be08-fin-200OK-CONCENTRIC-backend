package ok.backend.team.dto;
import lombok.Data;
import ok.backend.team.domain.entity.Team;

import java.time.LocalDateTime;

@Data
public class TeamResponseDto {
    private Long id;

    private Long chatroom_id;

    private String name;

    private Long creator_id;

    private LocalDateTime createAt;

    public TeamResponseDto(Team team) {

        this.id = team.getId();
        this.chatroom_id = team.getChatroom().getId();
        this.name = team.getName();
        this.creator_id = team.getCreator_id();
        this.createAt = team.getCreateAt();

    }

}
