package ok.backend.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ok.backend.chat.dto.req.ChatMessageRequestDto;
import ok.backend.chat.dto.res.ChatMessageResponseDto;
import ok.backend.chat.service.ChatMessageService;
import ok.backend.storage.domain.enums.StorageType;
import ok.backend.storage.dto.StorageResponseDto;
import ok.backend.storage.service.StorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Chat", description = "채팅 관리")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final StorageService storageService;

    @MessageMapping("/chat/{chatRoomId}")
    public void sendMessage(@DestinationVariable Long chatRoomId, ChatMessageRequestDto chatMessageRequestDto) {
        chatMessageService.sendMessage(chatRoomId, chatMessageRequestDto);
    }


    @PostMapping(value = "v1/api/chat/upload", consumes = "multipart/form-data")
    @Operation(summary = "채팅방 파일 업로드")
    public ResponseEntity<List<StorageResponseDto>> sendFileMessage(@DestinationVariable @RequestParam Long chatRoomId,
                                @RequestParam Long memberId, @RequestParam String nickname,
                                @RequestParam List<MultipartFile> files) throws IOException {
        List<StorageResponseDto> storageFiles =  storageService.uploadFileToStorage(chatRoomId, StorageType.CHAT, files);
        chatMessageService.sendFileMessage(chatRoomId, memberId, nickname, storageFiles);
        return ResponseEntity.ok(storageFiles);
    }

    @GetMapping(value = "v1/api/chat/{chatRoomId}")
    @Operation(summary = "채팅방 메세지 내역 조회")
    public ResponseEntity<List<ChatMessageResponseDto>> findAllChatMessage(@PathVariable Long chatRoomId) {
        List<ChatMessageResponseDto> chatMessageResponseDto =  chatMessageService.findAllChatMessage(chatRoomId);
        return ResponseEntity.ok(chatMessageResponseDto);
    }

}
