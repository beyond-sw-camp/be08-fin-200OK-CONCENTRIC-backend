package ok.backend.chat.service;

import lombok.RequiredArgsConstructor;
import ok.backend.chat.domain.entity.ChatMessage;
import ok.backend.chat.domain.repository.ChatMessageRepository;
import ok.backend.chat.dto.req.ChatMessageRequestDto;
import ok.backend.chat.dto.res.ChatMessageResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;

    public ChatMessageResponseDto saveChatMessage(final Long chatRoomId, final ChatMessageRequestDto chatMessageRequestDto) {
        final ChatMessage chatMessage = chatMessageRepository.save(
                ChatMessage.of(chatRoomId, chatMessageRequestDto.getMemberId(),
                        chatMessageRequestDto.getMessage(), chatMessageRequestDto.getFileUrl())
        );
        return ChatMessageResponseDto.of(chatMessage);
    }

    public List<ChatMessage> findAllChatMessage(final long chatRoomId) {
        return chatMessageRepository.findAll();
    }
}
