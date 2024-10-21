package ok.backend.chat.dto.res;

import lombok.*;
import ok.backend.chat.domain.entity.WebSocket;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class WebSocketResponseDto {

    private Long memberId;

    private String sessionId;

    private String lastConnect;

    public WebSocketResponseDto(WebSocket webSocket) {
        this.memberId = webSocket.getMemberId();
        this.sessionId = webSocket.getSessionId();
        this.lastConnect = webSocket.getLastConnect();
    }
}
