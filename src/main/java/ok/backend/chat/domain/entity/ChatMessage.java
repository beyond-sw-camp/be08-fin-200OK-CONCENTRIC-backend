package ok.backend.chat.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
@Document(collection = "chat_messages")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Builder
public class ChatMessage implements Serializable {

    @Id
    @Field(value = "chat_messages_id", targetType = FieldType.OBJECT_ID)
    private Long id;

    @Field("chat_room_id")
    private Long chatRoomId;

    @Field("user_id")
    private Long memberId;

    @Field("message")
    private String message;

    @Field("file_url")
    private String fileUrl;

    @Field("create_at")
    private String createAt;

    public static ChatMessage createMessage(Long chatRoomId, Long memberId, String message, String fileUrl) {
        return ChatMessage.builder()
                .chatRoomId(chatRoomId)
                .memberId(memberId)
                .message(message)
                .fileUrl(fileUrl)
                .createAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")).toString())
                .build();
    }
}
