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
    @Column(name = "chat_file_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_messages_id")
    private ChatMessages chatMessages;

    @Column(nullable = false)
    private String fileUrl;

    public static ChatFile createChatFile(ChatMessages chatMessages, String fileUrl) {
        return ChatFile.builder()
                .chatMessages(chatMessages)
                .fileUrl(fileUrl)
                .build();
    }
}
