package ok.backend.chat.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ok.backend.chat.dto.req.ChatMessageRequestDto;
import ok.backend.chat.dto.res.ChatMessageResponseDto;
import ok.backend.chat.service.ChatMessageService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    @MessageMapping("/chat/{chatRoomId}")
    @SendTo("/sub/chat/{chatRoomId}")
    public ChatMessageResponseDto broadcasting(final ChatMessageRequestDto chatMessageRequestDto,
                                               @DestinationVariable(value = "chatRoomId") final Long chatRoomId) {
        log.info("{chatRoomId: {}, request: {}}", chatRoomId, chatMessageRequestDto);
        // return: MongoDB에 채팅 메세지를 저장
        return chatMessageService.saveChatMessage(chatRoomId, chatMessageRequestDto);
    }
}
