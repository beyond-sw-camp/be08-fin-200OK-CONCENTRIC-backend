package ok.backend.common.config.web;

import lombok.RequiredArgsConstructor;
import ok.backend.chat.service.WebSocketService;
import ok.backend.common.security.util.JwtProvider;
import ok.backend.common.security.util.SecurityUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class StompHandler implements ChannelInterceptor {

    private final WebSocketService webSocketService;
    private final JwtProvider jwtProvider;
    private static final Logger logger = LoggerFactory.getLogger(StompHandler.class);

    // presend: STOMP 메세지가 전송되기 전에 호출되어 웹소켓 연결 시 토큰 검증
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        /**
         * 1. StompHeaderAccessor: Stomp 메세지의 헤더에 접근하는 클래스
         * 2. 전송된 Stomp 메세지의 Command가 CONNECT인지 검사
         * 3. StompHeaderAccessor로부터 Authorization 헤더의 JWT 토큰 추출(BEARER <- 이거 제거)
         * 4. jwtAuthenticationFilter로부터 유효한 토큰인지 확인
         */
        final StompHeaderAccessor stompHeaderAccessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(stompHeaderAccessor.getCommand())) {
            final String authorization = extractJwt(stompHeaderAccessor);

            String token = authorization.substring(7);
            boolean isValid = jwtProvider.validateToken(token);
            if (!isValid) {
                logger.error("Invalid JWT token: {}", token);
                throw new IllegalStateException("Invalid JWT token");
//                return message;
            }

            // 수동으로 Authentication 설정
            Authentication authentication = jwtProvider.getAuthentication(token);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
            Long memberId = Long.valueOf(securityUser.getUsername());
            String sessionId = stompHeaderAccessor.getSessionId();
            webSocketService.saveWebSocketConnection(memberId, sessionId);

        }

        else if (StompCommand.DISCONNECT.equals(stompHeaderAccessor.getCommand())) {
            String sessionId = stompHeaderAccessor.getSessionId();
            webSocketService.handleWebSocketDisconnection(sessionId);
            logger.info("WebSocket disconnected: sessionId = {}", sessionId);
        }

        else if (StompCommand.SEND.equals(stompHeaderAccessor.getCommand())) {
            if ("/pub/ping".equals(stompHeaderAccessor.getDestination())) {
                String sessionId = stompHeaderAccessor.getSessionId();
                webSocketService.updateLastConnectTime(sessionId);
            }
        }
        return message;
    }

    public String extractJwt(final StompHeaderAccessor accessor) {
        return accessor.getFirstNativeHeader("Authorization");
    }

}
