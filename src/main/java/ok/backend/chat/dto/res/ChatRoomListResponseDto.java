package ok.backend.chat.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ok.backend.chat.domain.entity.ChatRoomList;
import ok.backend.member.domain.entity.Member;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class ChatRoomListResponseDto {
    private Long memberId;
    private Long chatRoomId;
    public ChatRoomListResponseDto(ChatRoomList chatRoomList) {
        this.memberId = chatRoomList.getMember().getId();
        this.chatRoomId = chatRoomList.getChatRoom().getId();
    }
}
