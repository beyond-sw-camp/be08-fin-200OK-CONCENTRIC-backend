package ok.backend.chat.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "chat_messages")
public class ChatMessages {
    // TODO: 읽음 처리에 대한 칼럼 일단 제외함 찾아보고 추가하겠음
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_messages_id")
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

    @OneToMany(mappedBy = "chat_mesages")
    private List<ChatFile> chatFile;

    public static ChatMessages createMessage(String message, Member member, ChatRoom chatRoom) {
        return ChatMessages.builder()
                .message(message)
                .member(member)
                .chatRoom(chatRoom)
                .build();
    }
}
