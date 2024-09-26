package ok.backend.friendship.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class FriendshipRequestUpdateDto {

    @NotNull
    private Long id;

    @NotNull
    private Long receiverId;

    @NotNull
    private Long senderId;

    @NotNull
    private Boolean isAccept;

}
