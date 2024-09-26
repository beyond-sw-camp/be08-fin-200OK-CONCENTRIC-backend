package ok.backend.friendship.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import ok.backend.friendship.domain.entity.Friendship;
import ok.backend.friendship.domain.entity.FriendshipRequest;
import ok.backend.friendship.domain.repository.FriendshipCustomRepositoryImpl;
import ok.backend.friendship.domain.repository.FriendshipRepository;
import ok.backend.friendship.domain.repository.FriendshipRequestRepository;
import ok.backend.friendship.dto.FriendshipRequestDeleteRequestDto;
import ok.backend.friendship.dto.FriendshipRequestDto;
import ok.backend.friendship.dto.FriendshipResponseDto;
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
                .receiverId(friendshipRequestDto.getReceiverId())
                .build();

        friendshipRequestRepository.save(friendshipRequest);

        return toMember;
    }

    @Transactional
    public void deleteFriendshipRequest(FriendshipRequestDeleteRequestDto friendshipRequestDeleteRequestDto) {
        Member toMember = memberRepository.findById(friendshipRequestDeleteRequestDto.getReceiverId()).orElseThrow(() ->
                new RuntimeException("toMember not found"));
        Member fromMember = memberRepository.findById(friendshipRequestDeleteRequestDto.getSenderId()).orElseThrow(() ->
                new RuntimeException("fromMember not found"));

        if (friendshipRequestDeleteRequestDto.isAccept()) {
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
        }

        friendshipRequestRepository.deleteByMemberIdAndReceiverId(fromMember.getId(), toMember.getId());
    }

    public List<FriendshipResponseDto> getFriendshipMembers(Long memberId) {
        return friendshipCustomRepositoryImpl.findMembersByMemberId(memberId).stream()
                .map(FriendshipResponseDto::new)
                .collect(Collectors.toList());
    }
}
