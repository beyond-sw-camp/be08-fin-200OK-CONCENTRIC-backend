package ok.backend.chat.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import ok.backend.member.domain.entity.Member;
import org.hibernate.annotations.ColumnDefault;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "chat_room_list")
public class ChatRoomList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    @Column(nullable = false)
    @ColumnDefault("false")
    private Boolean bookmark;

    public static ChatRoomList createChatRoomList(Member member, ChatRoom chatRoom) {
        return ChatRoomList.builder()
                .member(member)
                .chatRoom(chatRoom)
                .bookmark(false)
                .build();
    }
}
