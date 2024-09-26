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
        registry.enableSimpleBroker("/sub");
        registry.setApplicationDestinationPrefixes("/pub");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 엔드포인트 추가 등록
        registry
                .setErrorHandler(stompExceptionHandler)
                .addEndpoint("/ws")
                .addInterceptors()
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Client로부터 들어오는 메세지를 처리하는 MessageChannel 구성 메소드
        registration.interceptors(stompHandler);
    }
}
