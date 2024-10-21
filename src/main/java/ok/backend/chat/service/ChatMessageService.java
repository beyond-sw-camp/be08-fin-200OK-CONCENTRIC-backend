package ok.backend.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ok.backend.chat.domain.entity.ChatMessage;
import ok.backend.chat.domain.repository.ChatMessageRepository;
import ok.backend.chat.domain.repository.ChatRoomListRepository;
import ok.backend.chat.dto.req.ChatMessageRequestDto;
import ok.backend.chat.dto.res.ChatMessageResponseDto;
import ok.backend.chat.dto.res.LastChatMessageResponseDto;
import ok.backend.common.exception.CustomException;
import ok.backend.common.security.util.SecurityUserDetailService;
import ok.backend.storage.domain.entity.StorageFile;
import ok.backend.storage.dto.StorageResponseDto;
import ok.backend.storage.service.StorageFileService;
import ok.backend.storage.service.StorageService;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static ok.backend.common.exception.ErrorCode.NOT_ACCESS_CHAT;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ChatMessageService {
    private final KafkaTemplate<String, ChatMessage> kafkaTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final StorageService storageService;
    private final StorageFileService storageFileService;
    private final ChatRoomListRepository chatRoomListRepository;
    private final SecurityUserDetailService securityUserDetailService;
    private final MongoTemplate mongoTemplate;

    // producer
    public void sendMessage(Long chatRoomId, ChatMessageRequestDto chatMessageRequestDto) {

        ChatMessage chatMessage = chatMessageRepository.save(ChatMessage.createMessage(
                chatRoomId, chatMessageRequestDto.getMemberId(), chatMessageRequestDto.getNickname(),
                chatMessageRequestDto.getMessage(), null, null));

        String topic = chatRoomId.toString();
        kafkaTemplate.send(topic, chatMessage);
    }

    public void sendFileMessage(Long chatRoomId, Long memberId, String nickname, List<StorageResponseDto> storageFiles) {
        for (StorageResponseDto storageResponse : storageFiles) {
            StorageFile storageFile = storageFileService.findByStorageIdAndId(
                    storageResponse.getStorageId(), storageResponse.getStorageFileId());

            Long fileId = storageFile.getId();
            String fileName = storageFile.getOriginalName();

            ChatMessage chatMessage = chatMessageRepository.save(ChatMessage.createMessage(
                    chatRoomId, memberId, nickname, null, fileId, fileName));

            String topic = chatRoomId.toString();
            kafkaTemplate.send(topic, chatMessage);
        }
    }

    // consumer
    @KafkaListener(topicPattern = ".*")
    public void consumeMessage(ChatMessage chatMessage) {
        simpMessagingTemplate.convertAndSend("/sub/chat/"+chatMessage.getChatRoomId().toString(), chatMessage);
    }

    public List<ChatMessageResponseDto> findAllChatMessage(Long chatRoomId) {
        if (chatRoomListRepository.findByMemberId(securityUserDetailService.getLoggedInMember().getId()).isEmpty()) {
            throw new CustomException(NOT_ACCESS_CHAT);
        }

        List<ChatMessage> chatMessages = chatMessageRepository.findByChatRoomId(chatRoomId);

        return chatMessages.stream()
                .map(chatMessage -> new ChatMessageResponseDto(
                        chatMessage.getChatRoomId(),
                        chatMessage.getMemberId(),
                        chatMessage.getNickname(),
                        chatMessage.getMessage(),
                        chatMessage.getFileId(),
                        chatMessage.getFileName(),
                        chatMessage.getCreateAt()
                ))
                .collect(Collectors.toList());
    }


    public List<LastChatMessageResponseDto> findLastChatMessage() {
        // Aggregation 파이프라인
        Aggregation result = Aggregation.newAggregation(
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "create_at")),
                Aggregation.group("chat_room_id")
                        .first("$$ROOT").as("latestMessage"),
                Aggregation.project()
                        .andExclude("_id")
                        .and("_id").as("chatRoomId")
                        .and("latestMessage.user_id").as("memberId")
                        .and("latestMessage.create_at").as("createAt")
        );

        AggregationResults<LastChatMessageResponseDto> lastChatMessageResponseDto = mongoTemplate.aggregate(
                result, "chat_messages", LastChatMessageResponseDto.class);
        return lastChatMessageResponseDto.getMappedResults();
    }
}
