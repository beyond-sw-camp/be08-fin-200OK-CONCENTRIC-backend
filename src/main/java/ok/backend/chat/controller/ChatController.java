package ok.backend.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ok.backend.chat.dto.req.ChatRoomRequestDto;
import ok.backend.chat.dto.res.ChatRoomListResponseDto;
import ok.backend.chat.dto.res.ChatRoomResponseDto;
import ok.backend.chat.service.ChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Chat", description = "채팅 관리")
@RequiredArgsConstructor
@RestController
@RequestMapping("v1/api/chat")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/create")
    @Operation(summary = "채팅방 생성")
    public ResponseEntity<ChatRoomResponseDto> createChat(@RequestParam("member") Long memberId,
                                                          @RequestBody @Valid ChatRoomRequestDto chatRoomRequestDto) {
        chatService.createChat(memberId, chatRoomRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/update")
    @Operation(summary = "채팅방 이름 수정")
    public ResponseEntity<Void> updateChat(@RequestParam("chatRoomId") Long chatRoomId,
                                       @RequestBody @Valid ChatRoomRequestDto chatRoomRequestDto) {
        chatService.updateChat(chatRoomId, chatRoomRequestDto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete")
    @Operation(summary = "채팅방 삭제")
    public ResponseEntity<Void> deleteChat(@RequestParam("chatRoomId") Long chatRoomId) {
        chatService.deleteChat(chatRoomId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/bookmark")
    @Operation(summary = "채팅방 즐겨찾기 설정")
    public ResponseEntity<Void> bookmarkChat(@RequestParam("memberId") Long memberId,
                                             @RequestParam("chatRoomId") Long chatRoomId) {
        chatService.bookmarkChat(memberId, chatRoomId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/join")
    @Operation(summary = "단체 채팅방 참여")
    public ResponseEntity<Void> joinChat(@RequestParam("memberId") Long memberId, @RequestParam("chatRoomId") Long chatRoomId) {
        chatService.joinChat(memberId, chatRoomId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/drop")
    @Operation(summary = "단체 채팅방 나가기")
    public ResponseEntity<Void> dropChat(@RequestParam("memberId") Long memberId,
                                         @RequestParam("chatRoomId") Long chatRoomId) {
        chatService.dropChat(memberId, chatRoomId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // TODO: 순환참조 방지
    @GetMapping("/participant")
    @Operation(summary = "채팅방 참여자 조회")
    public ResponseEntity<List<ChatRoomListResponseDto>> findChatParticipant(@RequestParam("memberId") Long memberId,
                                                                       @RequestParam("chatRoomId") Long chatRoomId) {
        List<ChatRoomListResponseDto> chatRoomListResponseDto = chatService.findChatParticipant(memberId, chatRoomId);
        return ResponseEntity.ok(chatRoomListResponseDto);
    }

    @GetMapping("/list")
    @Operation(summary = "채팅방 목록 조회")
    public ResponseEntity<List<ChatRoomResponseDto>> findChatRooms(@RequestParam("memberId") Long memberId) {
        List<ChatRoomResponseDto> chatRoomResponseDto = chatService.findChatRooms(memberId);
        return ResponseEntity.ok(chatRoomResponseDto);
    }

}
