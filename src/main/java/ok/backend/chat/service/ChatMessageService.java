package ok.backend.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ok.backend.chat.domain.entity.ChatMessage;
import ok.backend.chat.domain.entity.ChatRoom;
import ok.backend.chat.domain.repository.ChatMessageRepository;
import ok.backend.chat.domain.repository.ChatRoomListRepository;
import ok.backend.chat.domain.repository.ChatRoomRepository;
import ok.backend.chat.dto.req.ChatMessageRequestDto;
import ok.backend.common.exception.CustomException;
import ok.backend.common.security.util.SecurityUserDetailService;
import ok.backend.member.domain.entity.Member;
import ok.backend.member.domain.repository.MemberRepository;
import ok.backend.member.service.MemberService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static ok.backend.common.exception.ErrorCode.CHAT_NOT_FOUND;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ChatMessageService {
    private final KafkaTemplate<String, ChatMessage> kafkaTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    // producer
    public void sendMessage(Long chatRoomId, ChatMessageRequestDto chatMessageRequestDto) {

        ChatMessage chatMessage = chatMessageRepository.save(ChatMessage.createMessage(
                chatRoomId, chatMessageRequestDto.getMemberId(), chatMessageRequestDto.getMessage(),
                chatMessageRequestDto.getFileUrl()));

        String topic = chatRoomId.toString();
        kafkaTemplate.send(topic, chatMessage);
    }

    // consumer
    @KafkaListener(topicPattern = ".*")
    public void consumeMessage(ChatMessage chatMessage) {
        simpMessagingTemplate.convertAndSend("/sub/chat/"+chatMessage.getChatRoomId().toString(), chatMessage);
    }

}
