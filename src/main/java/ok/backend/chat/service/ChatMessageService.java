package ok.backend.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ok.backend.chat.domain.entity.ChatMessage;
import ok.backend.chat.domain.entity.ChatRoomList;
import ok.backend.chat.domain.repository.ChatMessageRepository;
import ok.backend.chat.dto.req.ChatMessageRequestDto;
import ok.backend.storage.domain.entity.StorageFile;
import ok.backend.storage.domain.enums.StorageType;
import ok.backend.storage.dto.StorageResponseDto;
import ok.backend.storage.service.StorageFileService;
import ok.backend.storage.service.StorageService;
import ok.backend.team.domain.entity.TeamList;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

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

    // producer
    public void sendMessage(Long chatRoomId, ChatMessageRequestDto chatMessageRequestDto) {

        ChatMessage chatMessage = chatMessageRepository.save(ChatMessage.createMessage(
                chatRoomId, chatMessageRequestDto.getMemberId(), chatMessageRequestDto.getMessage(),
                null));

        String topic = chatRoomId.toString();
        kafkaTemplate.send(topic, chatMessage);
    }

    public void sendFile(Long chatRoomId, Long memberId, List<MultipartFile> files) throws IOException {

        StorageResponseDto storageResponseDto = (StorageResponseDto) storageService.uploadFileToStorage(chatRoomId, StorageType.CHAT, files);
        StorageFile storageFile = storageFileService.findByStorageIdAndId(
                storageResponseDto.getStorageId(), storageResponseDto.getStorageFileId());

        String fileUrl = storageFile.getPath();
        ChatMessage chatMessage = chatMessageRepository.save(ChatMessage.createMessage(
                chatRoomId, memberId, null, fileUrl));

        String topic = chatRoomId.toString();
        kafkaTemplate.send(topic, chatMessage);
    }

    // consumer
    @KafkaListener(topicPattern = ".*")
    public void consumeMessage(ChatMessage chatMessage) {
        simpMessagingTemplate.convertAndSend("/sub/chat/"+chatMessage.getChatRoomId().toString(), chatMessage);
    }

}
