package ok.backend.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ok.backend.chat.domain.entity.WebSocket;
import ok.backend.chat.domain.repository.WebSocketRepository;
import ok.backend.chat.dto.res.WebSocketResponseDto;
import ok.backend.common.exception.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static ok.backend.common.exception.ErrorCode.CONNECTION_NOT_FOUND;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class WebSocketService extends TextWebSocketHandler {

    private final WebSocketRepository webSocketRepository;
    private final Map<String, Timer> sessionTimers = new ConcurrentHashMap<>();

    public WebSocketResponseDto findLastWebsocket(Long memberId) {

        List<WebSocket> webSocketList = webSocketRepository.findByMemberIdOrderByLastConnectDesc(memberId);
        if (webSocketList.isEmpty()) {
            throw new CustomException(CONNECTION_NOT_FOUND);
//            saveWebSocketConnection(memberId, null);
        }
        WebSocket mostRecentWebSocket = webSocketList.get(0);

        WebSocketResponseDto ResponseDto = new WebSocketResponseDto(mostRecentWebSocket);
        return ResponseDto;
    }

    // WebSocket 연결 시 MongoDB에 저장
    public void saveWebSocketConnection(Long memberId, String sessionId) {
        webSocketRepository.save(WebSocket.createLastConnect(memberId, sessionId, LocalDateTime.now().toString()));
        startPingTimeoutTimer(sessionId);
    }

    // ping 발생 시 타이머 리셋
    public void updateLastConnectTime(String sessionId) {
        List<WebSocket> webSocket = webSocketRepository.findBySessionIdOrderByLastConnectDesc(sessionId);

        if (webSocket != null) {
            resetPingTimeoutTimer(sessionId);
        }
    }

    // 웹소켓 연결 종료 시 호출
    public void handleWebSocketDisconnection(String sessionId) {
        List<WebSocket> webSocket = webSocketRepository.findBySessionIdOrderByLastConnectDesc(sessionId);

        if (webSocket != null) {
            webSocket.get(0).updateLastConnect(LocalDateTime.now().toString());
            webSocketRepository.save(webSocket.get(0));
            cancelPingTimeoutTimer(sessionId);
        }
    }

    // 타임아웃 발생 시 WebSocket 연결 종료 처리
    public void handlePingTimeout(String sessionId) {
        List<WebSocket> webSocket = webSocketRepository.findBySessionIdOrderByLastConnectDesc(sessionId);

        if (webSocket != null) {
            webSocket.get(0).updateLastConnect(LocalDateTime.now().toString());  // 마지막 연결 시간 기록
            webSocketRepository.save(webSocket.get(0));
        }
        handleWebSocketDisconnection(sessionId);
    }

    private void startPingTimeoutTimer(String sessionId) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handlePingTimeout(sessionId);  // 타임아웃 발생 시 처리
            }
        }, 60000);  // 60초 후 타임아웃
        sessionTimers.put(sessionId, timer);
    }

    private void resetPingTimeoutTimer(String sessionId) {
        cancelPingTimeoutTimer(sessionId);
        startPingTimeoutTimer(sessionId);
    }

    private void cancelPingTimeoutTimer(String sessionId) {
        Timer timer = sessionTimers.remove(sessionId);
        if (timer != null) {
            timer.cancel();
        }
    }

}
