package ok.backend.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ok.backend.chat.dto.res.WebSocketResponseDto;
import ok.backend.chat.service.WebSocketService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequiredArgsConstructor
@Tag(name = "Chat", description = "채팅 관리")
public class WebSocketController {

    private final WebSocketService webSocketService;

    @MessageMapping("/ping")
    @SendTo("/sub/pong")
    public String handlePing(String message) {
        return "pong";
    }

    @GetMapping(value = "v1/api/chat/last/connect")
    @Operation(summary = "마지막 웹소켓 연결 내역 조회")
    public ResponseEntity<WebSocketResponseDto> findLastWebsocket(@RequestParam("memberId") Long memberId) {
        WebSocketResponseDto webSocketResponseDto = webSocketService.findLastWebsocket(memberId);
        return ResponseEntity.ok(webSocketResponseDto);
    }
}
