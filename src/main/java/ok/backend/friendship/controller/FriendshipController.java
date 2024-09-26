package ok.backend.friendship.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ok.backend.friendship.dto.FriendshipRequestDeleteRequestDto;
import ok.backend.friendship.dto.FriendshipRequestDto;
import ok.backend.friendship.dto.FriendshipResponseDto;
import ok.backend.friendship.service.FriendshipService;
import ok.backend.member.domain.entity.Member;
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
    public ResponseEntity<String> createFriendship(@RequestBody FriendshipRequestDto friendshipRequestDto) {
        friendshipService.createFriendshipRequest(friendshipRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(description = "친구 요청 수락/거절 API")
    @DeleteMapping("/request/update")
    public ResponseEntity<String> deleteFriendshipRequest(@RequestBody FriendshipRequestDeleteRequestDto friendshipRequestDeleteRequestDto) {
        log.info(friendshipRequestDeleteRequestDto.toString());
        friendshipService.deleteFriendshipRequest(friendshipRequestDeleteRequestDto);

        return ResponseEntity.ok().build();
    }

    @Operation(description = "친구 목록 조회 API")
    @PostMapping("/list/accept/{memberId}")
    public ResponseEntity<List<FriendshipResponseDto>> getFriendsAcceptedList(@PathVariable Long memberId){
        List<FriendshipResponseDto> members = friendshipService.getFriendshipMembers(memberId);

        return ResponseEntity.ok(members);
    }
}
