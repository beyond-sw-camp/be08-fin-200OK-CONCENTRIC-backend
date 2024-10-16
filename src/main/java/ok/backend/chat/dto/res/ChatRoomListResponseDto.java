package ok.backend.chat.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ok.backend.chat.domain.entity.ChatRoom;
import ok.backend.chat.domain.entity.ChatRoomList;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class ChatRoomListResponseDto {

    private Long chatRoomId;

    private String nickname;

    private Boolean bookmark;

    private String userNickname;

    private String profileImageUrl;

    public ChatRoomListResponseDto(ChatRoomList chatRoomList, String userNickname, String profileImageUrl) {
        this.chatRoomId = chatRoomList.getChatRoom().getId();
        this.nickname = chatRoomList.getNickname();
        this.bookmark = chatRoomList.getBookmark();
        this.userNickname = userNickname;
        this.profileImageUrl = profileImageUrl;
    }
}
