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
import ok.backend.member.service.MemberService;
import ok.backend.storage.service.AwsFileService;
import ok.backend.storage.service.StorageFileService;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRequestRepository friendshipRequestRepository;

    private final FriendshipRepository friendshipRepository;

    private final MemberService memberService;

    private final StorageFileService storageFileService;

    private final FriendshipCustomRepositoryImpl friendshipCustomRepositoryImpl;

    private final SecurityUserDetailService securityUserDetailService;

    private final AwsFileService awsFileService;

    public Member createFriendshipRequest(String nickname) {
        Member member = memberService.findMemberById(securityUserDetailService.getLoggedInMember().getId());

        Member toMember = memberService.findMemberByNickname(nickname);

        Optional<Friendship> existingFriendship = friendshipRepository.findByMemberIdAndOtherId(member.getId(), toMember.getId());

        existingFriendship.ifPresent((friendship) -> {
            throw new CustomException(ErrorCode.DUPLICATE_SOCIAL);
        });

        Optional<FriendshipRequest> existingRequest = friendshipRequestRepository.findByMemberIdAndReceiverIdAndStatus(
                member.getId(), toMember.getId(), FriendshipRequestStatus.WAITING
        );

        existingRequest.ifPresent((friendshipRequest) -> {
            throw new CustomException(ErrorCode.DUPLICATE_SOCIAL);
        });

        FriendshipRequest friendshipRequest = FriendshipRequest.builder()
                .member(member)
                .receiverId(toMember.getId())
                .status(FriendshipRequestStatus.WAITING)
                .build();

        friendshipRequestRepository.save(friendshipRequest);

        return toMember;
    }

    public List<FriendshipRequestResponseDto> getFriendshipRequest() {
        List<FriendshipRequest> friendshipRequests = friendshipCustomRepositoryImpl
                .findFriendshipRequestsByReceiverId(securityUserDetailService.getLoggedInMember().getId());

        List<FriendshipRequestResponseDto> friendshipRequestResponseDtos = new ArrayList<>();

        for(FriendshipRequest friendshipRequest : friendshipRequests){
            String backgroundImage = null;
            String profileImage = null;

            if(friendshipRequest.getMember().getBackground() != null){
                backgroundImage = awsFileService.getUrl(friendshipRequest.getMember().getBackground());
            }

            if(friendshipRequest.getMember().getImageUrl() != null){
                profileImage = awsFileService.getUrl(friendshipRequest.getMember().getImageUrl());
            }

            FriendshipRequestResponseDto dto = FriendshipRequestResponseDto.builder()
                    .id(friendshipRequest.getId())
                    .memberId(friendshipRequest.getMember().getId())
                    .nickname(friendshipRequest.getMember().getNickname())
                    .createDate(friendshipRequest.getMember().getCreateDate())
                    .backgroundImage(backgroundImage)
                    .profileImage(profileImage)
                    .content(friendshipRequest.getMember().getContent())
                    .build();

            friendshipRequestResponseDtos.add(dto);
        }

        return friendshipRequestResponseDtos;
    }

    public void updateFriendshipRequest(FriendshipRequestUpdateDto friendshipRequestUpdateDto) {
        Member member = securityUserDetailService.getLoggedInMember();
        FriendshipRequest friendshipRequest = friendshipRequestRepository.findByMemberIdAndReceiverIdAndStatus(
                friendshipRequestUpdateDto.getSenderId(),
                member.getId(),
                FriendshipRequestStatus.WAITING
        ).orElseThrow(() -> new CustomException(ErrorCode.FRIENDSHIP_REQUEST_NOT_FOUND));

        if (friendshipRequestUpdateDto.getIsAccept()) {
            Member toMember = memberService.findMemberById(member.getId());
            Member fromMember = memberService.findMemberById(friendshipRequestUpdateDto.getSenderId());

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

    public List<FriendshipResponseDto> getFriendshipMembers() throws MalformedURLException {
        List<Member> memberList = friendshipCustomRepositoryImpl
                .findMembersByMemberId(securityUserDetailService.getLoggedInMember().getId());
        List<FriendshipResponseDto> friendshipResponseDtos = new ArrayList<>();

        for(Member member : memberList){
            String backgroundImage = null;
            String profileImage = null;

            if(member.getBackground() != null){
                backgroundImage = awsFileService.getUrl(member.getBackground());
            }

            if(member.getImageUrl() != null){
                profileImage = awsFileService.getUrl(member.getImageUrl());
            }

            FriendshipResponseDto dto = FriendshipResponseDto.builder()
                    .id(member.getId())
                    .nickname(member.getNickname())
                    .createDate(member.getCreateDate())
                    .backgroundImage(backgroundImage)
                    .profileImage(profileImage)
                    .content(member.getContent())
                    .build();

            friendshipResponseDtos.add(dto);
        }
        return friendshipResponseDtos;
    }

    public void deleteFriendship(Long otherId){
        Long memberId = securityUserDetailService.getLoggedInMember().getId();

        friendshipCustomRepositoryImpl.deleteFriendshipByMemberIdAndOtherId(memberId, otherId);
    }
}
