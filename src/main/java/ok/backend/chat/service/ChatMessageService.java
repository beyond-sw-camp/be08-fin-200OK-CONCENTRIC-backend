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
    // 안읽음 처리에 대한 고민.. -> Kafka 연결 후 consume event 관련한 메소드를 생성하게 되면, 여기서 처리해줄 수 있지 않을까
    // 채팅방 메세지 전송 유효성 검증 -> 로그인 후 채팅방 sub 하면서 권한 체크할 수 있도록
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
