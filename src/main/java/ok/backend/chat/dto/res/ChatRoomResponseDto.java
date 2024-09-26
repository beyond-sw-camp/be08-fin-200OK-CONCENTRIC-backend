package ok.backend.chat.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ok.backend.chat.domain.entity.ChatRoom;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class ChatRoomResponseDto {

    private Long id;

    private String name;

    private LocalDateTime createAt;

    private LocalDateTime updateAt;

    public ChatRoomResponseDto(ChatRoom chatRoom) {
        this.id = chatRoom.getId();
        this.name = chatRoom.getName();
        this.createAt = chatRoom.getCreateAt();
        this.updateAt = chatRoom.getUpdateAt();
    }
}