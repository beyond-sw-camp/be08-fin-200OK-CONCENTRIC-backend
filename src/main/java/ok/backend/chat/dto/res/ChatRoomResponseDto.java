package ok.backend.chat.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ok.backend.chat.domain.entity.ChatRoom;
import ok.backend.chat.domain.entity.Status;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class ChatRoomResponseDto {

    private Long id;

    private String name;

    private String status;

    private LocalDateTime createAt;

    private LocalDateTime updateAt;

    public ChatRoomResponseDto(ChatRoom chatRoom) {
        this.id = chatRoom.getId();
        this.name = chatRoom.getName();
        this.status = chatRoom.getStatus().toString();
        this.createAt = chatRoom.getCreateAt();
        this.updateAt = chatRoom.getUpdateAt();
    }
}