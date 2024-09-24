package ok.backend.chat.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ok.backend.chat.dto.req.ChatMessageRequestDto;
import ok.backend.chat.dto.res.ChatMessageResponseDto;
import ok.backend.chat.service.ChatMessageService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    // ChatMessageService: MongoDB에서 채팅 메세지를 저장하는 기능
    private final ChatMessageService chatMessageService;

    @MessageMapping("/pub/chat/{chatRoomId}")
    @SendTo("/sub/chat/{chatRoomId}")
    public ChatMessageResponseDto broadcasting(final ChatMessageRequestDto chatMessageRequestDto, // SendMessage ReqDto
                                               @DestinationVariable(value = "chatRoomId") final Long chatRoomId) {
        log.info("{chatRoomId: {}, request: {}}", chatRoomId, chatMessageRequestDto);
        return chatMessageService.saveChatMessage(chatRoomId, chatMessageRequestDto);
    }
}
