package ok.backend.friendship.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import ok.backend.common.exception.CustomException;
import ok.backend.common.exception.ErrorCode;
import ok.backend.common.security.util.SecurityUserDetailService;
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

    private final SecurityUserDetailService securityUserDetailService;

    @Transactional
    public Member createFriendshipRequest(FriendshipRequestDto friendshipRequestDto) {
        Member member = memberRepository.findById(securityUserDetailService.getLoggedInMember().getId()).orElseThrow(() ->
                new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Member toMember = memberRepository.findById(friendshipRequestDto.getReceiverId()).orElseThrow(() ->
                new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        FriendshipRequest friendshipRequest = FriendshipRequest.builder()
                .member(member)
                .receiverId(toMember.getId())
                .status(FriendshipRequestStatus.WAITING)
                .build();

        friendshipRequestRepository.save(friendshipRequest);

        return toMember;
    }

    public List<FriendshipRequestResponseDto> getFriendshipRequest() {
        return friendshipCustomRepositoryImpl
                .findFriendshipRequestsByReceiverId(securityUserDetailService.getLoggedInMember().getId())
                .stream()
                .map(FriendshipRequestResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateFriendshipRequest(FriendshipRequestUpdateDto friendshipRequestUpdateDto) {
        Member member = securityUserDetailService.getLoggedInMember();
        FriendshipRequest friendshipRequest = friendshipRequestRepository.findByMemberIdAndReceiverIdAndStatus(
                friendshipRequestUpdateDto.getSenderId(),
                member.getId(),
                FriendshipRequestStatus.WAITING
        ).orElseThrow(() -> new CustomException(ErrorCode.FRIENDSHIP_REQUEST_NOT_FOUND));

        if (friendshipRequestUpdateDto.getIsAccept()) {
            Member toMember = memberRepository.findById(member.getId()).orElseThrow(() ->
                    new CustomException(ErrorCode.MEMBER_NOT_FOUND));
            Member fromMember = memberRepository.findById(friendshipRequestUpdateDto.getSenderId()).orElseThrow(() ->
                    new CustomException(ErrorCode.MEMBER_NOT_FOUND));

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

    public List<FriendshipResponseDto> getFriendshipMembers() {
        return friendshipCustomRepositoryImpl
                .findMembersByMemberId(securityUserDetailService.getLoggedInMember().getId()).stream()
                .map(FriendshipResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteFriendship(FriendshipDeleteRequestDto friendshipDeleteRequestDto){
        Long memberId = securityUserDetailService.getLoggedInMember().getId();
        Long otherId = friendshipDeleteRequestDto.getOtherId();

        friendshipCustomRepositoryImpl.deleteFriendshipByMemberIdAndOtherId(memberId, otherId);
    }
}
