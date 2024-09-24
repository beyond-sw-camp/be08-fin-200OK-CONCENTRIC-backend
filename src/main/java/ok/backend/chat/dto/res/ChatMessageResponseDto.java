package ok.backend.chat.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ok.backend.chat.domain.entity.ChatMessage;

@Getter
@AllArgsConstructor
public class ChatMessageResponseDto {
    private Long chatRoomId;
    private Long memberId;
    private String message;
    private String fileUrl;

    public static ChatMessageResponseDto of(ChatMessage chatMessage) {
        return new ChatMessageResponseDto(chatMessage.getChatRoomId(), chatMessage.getMemberId(),
                chatMessage.getMessage(), chatMessage.getFileUrl());
    }
}
