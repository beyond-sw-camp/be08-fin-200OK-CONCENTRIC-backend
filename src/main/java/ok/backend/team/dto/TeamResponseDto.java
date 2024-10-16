package ok.backend.team.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ok.backend.team.domain.entity.Team;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TeamResponseDto {
    private Long id;

//    private Long chatroom_id;

    private String name;

    private Long creatorId;

    private LocalDateTime createAt;

    private String imageUrl;

    public TeamResponseDto(Team team) {

        this.id = team.getId();
        //this.chatroom_id = team.getChatroom().getId();
        this.name = team.getName();
        this.creatorId = team.getCreatorId();
        this.createAt = team.getCreateAt();
        this.imageUrl = team.getImageUrl();

    }

}
