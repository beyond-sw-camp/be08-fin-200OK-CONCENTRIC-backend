package ok.backend.common.config.web;

import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class StompHandler implements ChannelInterceptor {
    // WebSocket 연결 시 헤더에서 JWT token 유효성 검증
//    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // presend: STOMP 메세지가 전송되기 전에 호출되어 웹소켓 연결 시 토큰 검증
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        /**
         * 1. StompHeaderAccessor: Stomp 메세지의 헤더에 접근하는 클래스
         * 2. 전송된 Stomp 메세지의 Command가 CONNECT인지 검사
         * 3. StompHeaderAccessor로부터 Authorization 헤더의 JWT 토큰 추출
         * 4. jwtAuthenticationFilter로부터 유효한 토큰인지 확인
         */
        final StompHeaderAccessor stompHeaderAccessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(stompHeaderAccessor.getCommand())) {
            final String authorization = extractJwt(stompHeaderAccessor);

//            jwtAuthenticationFilter.검증 메소드(authorization);
        }
        return message;
    }

    // STOMP 헤더에서 Authorization 값 추출
    public String extractJwt(final StompHeaderAccessor accessor) {
        return accessor.getFirstNativeHeader("Authorization");
    }
}
