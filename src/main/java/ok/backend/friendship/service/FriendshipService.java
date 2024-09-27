package ok.backend.friendship.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import ok.backend.friendship.domain.entity.Friendship;
import ok.backend.friendship.domain.entity.FriendshipRequest;
import ok.backend.friendship.domain.enums.FriendshipRequestStatus;
import ok.backend.friendship.domain.repository.FriendshipCustomRepositoryImpl;
import ok.backend.friendship.domain.repository.FriendshipRepository;
import ok.backend.friendship.domain.repository.FriendshipRequestRepository;
import ok.backend.friendship.dto.*;
import ok.backend.member.domain.entity.Member;
import ok.backend.member.domain.repository.MemberRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRequestRepository friendshipRequestRepository;

    private final FriendshipRepository friendshipRepository;

    private final MemberRepository memberRepository;

    private final FriendshipCustomRepositoryImpl friendshipCustomRepositoryImpl;

    @Transactional
    public Member createFriendshipRequest(FriendshipRequestDto friendshipRequestDto) {
        Member member = memberRepository.findById(friendshipRequestDto.getUserId()).orElseThrow(() ->
                new RuntimeException("Member not found"));

        Member toMember = memberRepository.findById(friendshipRequestDto.getReceiverId()).orElseThrow(() ->
                new RuntimeException("toMember not found"));

        FriendshipRequest friendshipRequest = FriendshipRequest.builder()
                .member(member)
                .receiverId(toMember.getId())
                .status(FriendshipRequestStatus.WAITING)
                .build();

        friendshipRequestRepository.save(friendshipRequest);

        return toMember;
    }

    public List<FriendshipRequestResponseDto> getFriendshipRequest(Long memberId) {
        return friendshipCustomRepositoryImpl.findFriendshipRequestsByReceiverId(memberId).stream()
                .map(FriendshipRequestResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateFriendshipRequest(FriendshipRequestUpdateDto friendshipRequestUpdateDto) {
        FriendshipRequest friendshipRequest = friendshipRequestRepository.findByMemberIdAndReceiverIdAndStatus(
                friendshipRequestUpdateDto.getSenderId(),
                friendshipRequestUpdateDto.getReceiverId(),
                FriendshipRequestStatus.WAITING
        ).orElseThrow(() -> new RuntimeException("friendshipRequest not found"));

        if (friendshipRequestUpdateDto.getIsAccept()) {
            Member toMember = memberRepository.findById(friendshipRequestUpdateDto.getReceiverId()).orElseThrow(() ->
                    new RuntimeException("toMember not found"));
            Member fromMember = memberRepository.findById(friendshipRequestUpdateDto.getSenderId()).orElseThrow(() ->
                    new RuntimeException("fromMember not found"));

            Friendship fromFriendship = Friendship.builder()
                    .member(fromMember)
                    .otherId(toMember.getId())
                    .build();

            friendshipRepository.save(fromFriendship);

            Friendship toFriendship = Friendship.builder()
                    .member(toMember)
                    .otherId(fromMember.getId())
                    .build();

            friendshipRepository.save(toFriendship);

            friendshipRequest.updateStatus(FriendshipRequestStatus.ACCEPTED);
            friendshipRequestRepository.save(friendshipRequest);
        } else {
            friendshipRequest.updateStatus(FriendshipRequestStatus.REJECTED);
            friendshipRequestRepository.save(friendshipRequest);
        }

    }

    public List<FriendshipResponseDto> getFriendshipMembers(Long memberId) {
        return friendshipCustomRepositoryImpl.findMembersByMemberId(memberId).stream()
                .map(FriendshipResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteFriendship(FriendshipDeleteRequestDto friendshipDeleteRequestDto){
        Long memberId = friendshipDeleteRequestDto.getMemberId();
        Long otherId = friendshipDeleteRequestDto.getOtherId();

        friendshipCustomRepositoryImpl.deleteFriendshipByMemberIdAndOtherId(memberId, otherId);
    }
}
