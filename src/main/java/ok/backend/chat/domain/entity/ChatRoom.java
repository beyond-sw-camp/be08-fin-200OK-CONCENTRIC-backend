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
@Table(name = "chat_room")
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Boolean bookmark;

    @CreationTimestamp
    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt;

    @CreationTimestamp
    @Column(name = "update_at", nullable = false)
    private LocalDateTime updateAt;

    @OneToMany(mappedBy = "chatRoom")
    private List<ChatMessage> chatMessages;

    @OneToMany(mappedBy = "chatRoom")
    private List<ChatRoomList> chatRoomList;

    public static ChatRoom createChatRoom(String name) {
        return ChatRoom.builder()
                .name(name)
                .bookmark(false)
                .build();
    }
}