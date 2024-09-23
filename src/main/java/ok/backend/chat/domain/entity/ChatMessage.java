package ok.backend.chat.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import ok.backend.member.domain.entity.Member;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "chat_messages")
public class ChatMessage {
    // TODO: 읽음 처리에 대한 칼럼 일단 제외함 찾아보고 추가하겠음
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String message;

    @CreationTimestamp
    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    @OneToOne(mappedBy = "chatMessage")
    private ChatFile chatFile;

    public static ChatMessage createMessage(String message, Member member, ChatRoom chatRoom) {
        return ChatMessage.builder()
                .message(message)
                .member(member)
                .chatRoom(chatRoom)
                .build();
    }
}
