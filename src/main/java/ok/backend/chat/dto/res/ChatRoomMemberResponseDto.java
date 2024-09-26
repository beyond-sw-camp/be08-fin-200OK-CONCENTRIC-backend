package ok.backend.chat.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ok.backend.chat.domain.entity.ChatRoomList;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class ChatRoomMemberResponseDto {

    private Long chatRoomId;

    private Long memberId;

    private String nickname;

    private String imageUrl;

    public ChatRoomMemberResponseDto(ChatRoomList chatRoomList) {
        this.chatRoomId = chatRoomList.getChatRoom().getId();
        this.memberId = chatRoomList.getMember().getId();
        this.nickname = chatRoomList.getMember().getNickname();
        this.imageUrl = chatRoomList.getMember().getImageUrl();
    }

}
