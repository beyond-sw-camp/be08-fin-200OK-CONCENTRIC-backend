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

import java.net.MalformedURLException;
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
    public ResponseEntity<String> createFriendshipRequest(@RequestParam String nickname) {
        friendshipService.createFriendshipRequest(nickname);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(description = "친구 요청 조회 API")
    @GetMapping("/request/list")
    public ResponseEntity<List<FriendshipRequestResponseDto>> getFriendshipRequest() {
       List<FriendshipRequestResponseDto> friendshipRequests = friendshipService.getFriendshipRequest();

       return ResponseEntity.ok(friendshipRequests);
    }

    @Operation(description = "친구 요청 수락/거절 API")
    @PutMapping("/request/update")
    public ResponseEntity<String> updateFriendshipRequest(@RequestBody FriendshipRequestUpdateDto friendshipRequestUpdateDto) {
        friendshipService.updateFriendshipRequest(friendshipRequestUpdateDto);

        return ResponseEntity.ok().build();
    }

    @Operation(description = "친구 목록 조회 API")
    @GetMapping("/list")
    public ResponseEntity<List<FriendshipResponseDto>> getFriendsAcceptedList() throws MalformedURLException {
        List<FriendshipResponseDto> members = friendshipService.getFriendshipMembers();

        return ResponseEntity.ok(members);
    }

    @Operation(summary = "친구 삭제 API")
    @DeleteMapping("/delete/{otherId}")
    public ResponseEntity<String> deleteFriendship(@PathVariable Long otherId) {
        friendshipService.deleteFriendship(otherId);

        return ResponseEntity.ok().build();
    }
}
