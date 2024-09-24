package ok.backend.chat.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.time.LocalDateTime;

@Getter
@Document(collection = "chat_messages")
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ChatMessage {
    /**
     * TODO: 파일 관련된 설정도 여기에서 처리하길래 한번 넣어봤는데 고민해봐야 할 듯..
     * 안읽음 처리 관련한 건 추후에 생각해보겠음
     */

    @Id
    @Field(value = "chat_messages_id", targetType = FieldType.OBJECT_ID)
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Field("chat_room_id")
    private Long chatRoomId;

    @Field("user_id")
    private Long memberId;

    @Field("message")
    private String message;

    @Field("file_url")
    private String fileUrl;

    @CreatedDate
    @Field("create_at")
    private LocalDateTime createAt;

    // 생성자 처리
    public ChatMessage(Long chatRoomId, Long memberId, String message, String fileUrl) {
        this.chatRoomId = chatRoomId;
        this.memberId = memberId;
        this.message = message;
        this.fileUrl = fileUrl;
    }

    // 정적 팩토리 메소드
    public static ChatMessage of(Long chatRoomId, Long memberId, String message, String fileUrl) {
        return new ChatMessage(chatRoomId, memberId, message, fileUrl);
    }
}
