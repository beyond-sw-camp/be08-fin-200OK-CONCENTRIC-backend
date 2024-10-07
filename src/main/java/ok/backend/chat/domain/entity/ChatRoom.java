package ok.backend.chat.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Table(name = "chat_room")
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Type type;

    @Column(name = "team_id")
    private Long teamId;

    @CreationTimestamp
    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt;

    @CreationTimestamp
    @Column(name = "update_at", nullable = false)
    private LocalDateTime updateAt;

    @Column(name = "is_active", nullable = false)
    @ColumnDefault("true")
    private Boolean isActive;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatRoomList> chatRoomList;

    public static ChatRoom createChatRoom(String name, Type type, Long teamId, Boolean isActive) {
        return ChatRoom.builder()
                .name(name)
                .type(type)
                .teamId(teamId)
                .isActive(isActive)
                .build();
    }

    public void updateIsActive(boolean isActive) {
        this.isActive = isActive;
    }
}
