package ok.backend.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ok.backend.chat.domain.entity.ChatMessage;
import ok.backend.chat.domain.entity.ChatRoom;
import ok.backend.chat.domain.repository.ChatMessageRepository;
import ok.backend.chat.domain.repository.ChatRoomRepository;
import ok.backend.chat.dto.req.ChatMessageRequestDto;
import ok.backend.chat.dto.res.ChatMessageResponseDto;
import ok.backend.member.domain.entity.Member;
import ok.backend.member.domain.repository.MemberRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ChatMessageService {
    // 채팅방 메세지 전송 유효성 검증 -> 로그인 후 채팅방 sub 하면서 권한 체크할 수 있도록
    private final KafkaTemplate<String, ChatMessage> kafkaTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;

    private final ObjectMapper objectMapper;

    private final SimpMessagingTemplate simpMessagingTemplate;

    // producer
    // 검증 로직 필요
    public void sendMessage(Long chatRoomId, ChatMessageRequestDto chatMessageRequestDto) {
        Member member = memberRepository.findById(chatMessageRequestDto.getMemberId()).orElseThrow(
                () -> new IllegalArgumentException("Member not found")
        );
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow(
                () -> new IllegalArgumentException("Chat room not found")
        );

        ChatMessage chatMessage = chatMessageRepository.save(ChatMessage.createMessage(chatRoomId, chatMessageRequestDto.getMemberId(), chatMessageRequestDto.getMessage(), chatMessageRequestDto.getFileUrl()));

        System.out.println("Kafka topic: " + chatRoomId);
        String topic = chatRoomId.toString();
        kafkaTemplate.send(topic, chatMessage);

    }

    // consumer
    @KafkaListener(topicPattern = ".*")
    public void consumeMessage(ChatMessage chatMessage) {
        System.out.println("Received message: " + chatMessage);
        simpMessagingTemplate.convertAndSend("/sub/chat/"+chatMessage.getChatRoomId().toString(), chatMessage);
    }
}
