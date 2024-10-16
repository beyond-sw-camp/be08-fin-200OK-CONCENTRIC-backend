package ok.backend.chat.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class ChatMessageRequestDto {
    @NotNull
    private Long memberId;

    private String nickname;

    private String message;

    private String fileUrl;
}
