package ok.backend.chat.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ok.backend.chat.dto.req.ChatMessageRequestDto;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
@Builder
@AllArgsConstructor
@Setter
public class ChatMessageResponseDto {
    private Long chatRoomId;
    private Long memberId;
    private String message;
    private String fileUrl;
    private String createAt;

    public ChatMessageResponseDto(Long chatRoomId, ChatMessageRequestDto chatMessageRequestDto) {
        this.chatRoomId = chatRoomId;
        this.memberId = chatMessageRequestDto.getMemberId();
        this.message = chatMessageRequestDto.getMessage();
        this.fileUrl = chatMessageRequestDto.getMessage();
        this.createAt = LocalDateTime.now(ZoneId.of("Asia/Seoul")).toString();
    }
}
