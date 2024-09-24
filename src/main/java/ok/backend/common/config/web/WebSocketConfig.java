package ok.backend.common.config.web;

import lombok.RequiredArgsConstructor;
import ok.backend.chat.exception.StompExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // WebSocket Stomp로 연결하는 흐름에 대한 제어를 위한 interceptor. JWT 인증에 사용
    private final StompHandler stompHandler;
    // WebSocket 연결 시 발생하는 exception 핸들링 목적의 클래스
    private final StompExceptionHandler stompExceptionHandler;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 해당 파라미터의 접두사가 붙은 구독자에게 메세지를 보냄
        registry.enableSimpleBroker("/sub");
        registry.setApplicationDestinationPrefixes("/pub"); // e.g. /pub/chat/ChatRoomId
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 엔드포인트 추가 등록
        registry
                .setErrorHandler(stompExceptionHandler) // exception handler
                // 클라이언트는 /ws/chat 엔드포인트를 통해 연결하고, /pub 및 /sub 접두사를 사용하여 메시지를 송수신
                .addEndpoint("/ws/chat")
                .addInterceptors()
                .setAllowedOriginPatterns("*"); // CORS 설정을 모두 허용
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Client로부터 들어오는 메세지를 처리하는 MessageChannel 구성 메소드
        // TCP handshake 시 JWT 인증
        registration.interceptors(stompHandler);
    }
}
