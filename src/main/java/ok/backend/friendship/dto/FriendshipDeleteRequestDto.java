package ok.backend.friendship.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class FriendshipDeleteRequestDto {

//    @NotNull
//    private Long memberId;

    @NotNull
    private Long otherId;
}
