package ok.backend.chat.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
@AllArgsConstructor
@Setter
public class LastChatMessageResponseDto {
    private Long chatRoomId;
    private Long memberId;
    private String createAt;
}
