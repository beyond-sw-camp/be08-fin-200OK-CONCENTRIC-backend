package ok.backend.friendship.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class FriendshipRequestDto {

//    @NotNull
//    private Long userId;

    @NotNull
    private Long receiverId;
}
