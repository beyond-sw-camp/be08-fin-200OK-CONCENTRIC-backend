package ok.backend.friendship.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ok.backend.friendship.dto.*;
import ok.backend.friendship.service.FriendshipService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Friendship", description = "친구 관리")
@RestController
@RequiredArgsConstructor
@RequestMapping("v1/api/friendship")
@Slf4j
public class FriendshipController {

    private final FriendshipService friendshipService;

    @Operation(description = "친구 요청 생성 API")
    @PostMapping("/request/create")
    public ResponseEntity<String> createFriendshipRequest(@RequestBody FriendshipRequestDto friendshipRequestDto) {
        friendshipService.createFriendshipRequest(friendshipRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(description = "친구 요청 조회 API")
    @GetMapping("/request/list/{memberId}")
    public ResponseEntity<List<FriendshipRequestResponseDto>> getFriendshipRequest(@PathVariable Long memberId) {
       List<FriendshipRequestResponseDto> friendshipRequests = friendshipService.getFriendshipRequest(memberId);

       return ResponseEntity.ok(friendshipRequests);
    }

    @Operation(description = "친구 요청 수락/거절 API")
    @PutMapping("/request/update")
    public ResponseEntity<String> updateFriendshipRequest(@RequestBody FriendshipRequestUpdateDto friendshipRequestUpdateDto) {
        log.info(friendshipRequestUpdateDto.toString());
        friendshipService.updateFriendshipRequest(friendshipRequestUpdateDto);

        return ResponseEntity.ok().build();
    }

    @Operation(description = "친구 목록 조회 API")
    @GetMapping("/list/accept/{memberId}")
    public ResponseEntity<List<FriendshipResponseDto>> getFriendsAcceptedList(@PathVariable Long memberId){
        List<FriendshipResponseDto> members = friendshipService.getFriendshipMembers(memberId);

        return ResponseEntity.ok(members);
    }

    @Operation(summary = "친구 삭제 API")
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteFriendship(@RequestBody FriendshipDeleteRequestDto friendshipDeleteRequestDto) {
        friendshipService.deleteFriendship(friendshipDeleteRequestDto);

        return ResponseEntity.ok().build();
    }
}
