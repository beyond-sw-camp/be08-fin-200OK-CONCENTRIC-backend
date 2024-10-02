package ok.backend.chat.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import ok.backend.member.domain.entity.Member;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.mongodb.core.mapping.DBRef;
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
@Setter
public class ChatMessage implements Serializable {
    /**
     * TODO: 파일 관련된 설정도 여기에서 처리하길래 한번 넣어봤는데 고민해봐야 할 듯..
     * 안읽음 처리 관련한 건 추후에 생각해보겠음
     */

    @Id
    @Field(value = "chat_messages_id", targetType = FieldType.OBJECT_ID)
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @DBRef
//    @Field("chat_room_id")
//    @JsonIgnore
//    private ChatRoom chatRoom;
//
//    @DBRef
//    @Field("user_id")
//    @JsonIgnore
//    private Member member;

    @Field("chat_room_id")
    private Long chatRoomId;

    @Field("user_id")
    private Long memberId;


    @Field("message")
    private String message;

    @Field("file_url")
    private String fileUrl;

//    @Field("create_at")
//    @CreatedDate
//    private LocalDateTime createAt;
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
