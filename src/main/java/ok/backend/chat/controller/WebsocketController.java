package ok.backend.chat.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebsocketController {

    @MessageMapping("/ping")
    @SendTo("/sub/pong")
    public String handlePing(String message) {
        // 클라이언트에서 받은 ping 메시지에 응답하는 pong 메시지
        return "pong";
    }
}
