package ok.backend.chat.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.io.Serializable;

@Getter
@Document(collection = "websocket")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Builder
public class WebSocket implements Serializable {
    @Id
    @Field(value = "websocket_id", targetType = FieldType.OBJECT_ID)
    private String id;

    @Field("user_id")
    private Long memberId;

    @Field("session_id")
    private String sessionId;

    @Field("last_connect")
    private String lastConnect;

    public static WebSocket createLastConnect(Long memberId, String sessionId, String lastConnect) {
        return WebSocket.builder()
                .memberId(memberId)
                .sessionId(sessionId)
                .lastConnect(lastConnect)
                .build();
    }

    public void updateLastConnect(String lastConnect) {
        this.lastConnect = lastConnect;
    }
}
