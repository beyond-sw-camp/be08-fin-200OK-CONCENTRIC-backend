package ok.backend.team.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class TeamInviteAcceptRequestDto {

    @NotNull
    private String key;

    @NotNull
    private Long teamId;

    @NotNull
    private String email;
}
