package ok.backend.friendship.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Data
public class FriendshipRequestDeleteRequestDto {

    @NotNull
    private Long receiverId;

    @NotNull
    private Long senderId;

    @NotNull
    private boolean isAccept;

}
