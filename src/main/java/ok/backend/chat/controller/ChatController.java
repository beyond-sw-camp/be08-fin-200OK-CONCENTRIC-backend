package ok.backend.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ok.backend.chat.dto.req.ChatRoomListRequestDto;
import ok.backend.chat.dto.req.ChatRoomRequestDto;
import ok.backend.chat.dto.res.ChatRoomMemberResponseDto;
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
    public ResponseEntity<ChatRoomResponseDto> createChat(@RequestParam("friendId") Long friendId,
                                                          @RequestBody @Valid ChatRoomRequestDto chatRoomRequestDto) {
        ChatRoomResponseDto chatRoomResponseDto = chatService.createChat(friendId, chatRoomRequestDto);
        return ResponseEntity.ok(chatRoomResponseDto);
//        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

//    @PostMapping("/create/team")
//    @Operation(summary = "단체 채팅방 생성")
//    public ResponseEntity<HttpStatus> createTeamChat(@RequestParam("teamId") Long teamId) {
//        chatService.createTeamChat(teamId);
//        return ResponseEntity.ok(HttpStatus.CREATED);
//    }

    @PutMapping("/rename")
    @Operation(summary = "채팅방 이름 수정")
    public ResponseEntity<Void> renameChat(@RequestBody @Valid ChatRoomListRequestDto chatRoomListRequestDto) {
        chatService.renameChat(chatRoomListRequestDto);
        return ResponseEntity.noContent().build();
    }

//    @DeleteMapping("/delete")
//    @Operation(summary = "채팅방 삭제")
//    public ResponseEntity<Void> deleteChat(@RequestParam("teamId") Long teamId) {
//        chatService.deleteChat(teamId);
//        return ResponseEntity.noContent().build();
//    }

    @PutMapping("/bookmark")
    @Operation(summary = "채팅방 즐겨찾기 설정")
    public ResponseEntity<Void> bookmarkChat(@RequestParam("chatRoomId") Long chatRoomId) {
        chatService.bookmarkChat(chatRoomId);
        return ResponseEntity.noContent().build();
    }

//    @PostMapping("/join")
//    @Operation(summary = "단체 채팅방 참여")
//    public ResponseEntity<HttpStatus> joinChat(@RequestParam("teamId") Long teamId) {
//        chatService.joinChat(teamId);
//        return ResponseEntity.ok(HttpStatus.CREATED);
//    }

    @DeleteMapping("/drop")
    @Operation(summary = "채팅방 나가기")
    public ResponseEntity<Void> dropChat(@RequestParam("chatRoomId") Long chatRoomId) {
        chatService.dropChat(chatRoomId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/participant")
    @Operation(summary = "채팅방 참여자 조회")
    public ResponseEntity<List<ChatRoomMemberResponseDto>> findChatParticipant(@RequestParam("chatRoomId") Long chatRoomId) {
        List<ChatRoomMemberResponseDto> chatRoomMemberResponseDto = chatService.findChatParticipant(chatRoomId);
        return ResponseEntity.ok(chatRoomMemberResponseDto);
    }

    @GetMapping("/list")
    @Operation(summary = "채팅방 목록 조회")
    public ResponseEntity<List<ChatRoomListResponseDto>> findChatRooms() {
        List<ChatRoomListResponseDto> chatRoomListResponseDto = chatService.findChatRooms();
        return ResponseEntity.ok(chatRoomListResponseDto);
    }

}
