package ok.backend.chat.dto.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class ChatMessageRequestDto {
    private Long memberId;
    private String message;
    private String fileUrl;
}