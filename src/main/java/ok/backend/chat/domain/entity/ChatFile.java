package ok.backend.chat.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "chat_file")
public class ChatFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_messages_id")
    private ChatMessage chatMessage;

    @Column(nullable = false)
    private String fileUrl;

    public static ChatFile createChatFile(ChatMessage chatMessage, String fileUrl) {
        return ChatFile.builder()
                .chatMessage(chatMessage)
                .fileUrl(fileUrl)
                .build();
    }
}
