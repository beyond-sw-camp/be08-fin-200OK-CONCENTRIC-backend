package ok.backend.chat.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ok.backend.chat.domain.entity.ChatMessage;
import ok.backend.chat.domain.entity.ChatRoomList;
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
    private LocalDateTime createAt;

//    public ChatMessageResponseDto(ChatMessage chatMessage) {
//        this.chatRoomId = chatMessage.getChatRoom().getId();
//        this.memberId = chatMessage.getMember().getId();
//        this.message = chatMessage.getMessage();
//        this.fileUrl = chatMessage.getFileUrl();
//        this.createAt = chatMessage.getCreateAt().toString();
//    }

    public ChatMessageResponseDto(Long chatRoomId, ChatMessageRequestDto chatMessageRequestDto) {
        this.chatRoomId = chatRoomId;
        this.memberId = chatMessageRequestDto.getMemberId();
        this.message = chatMessageRequestDto.getMessage();
        this.fileUrl = chatMessageRequestDto.getMessage();
        this.createAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    }
}
