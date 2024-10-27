package ok.backend.friendship.service;

import ok.backend.common.exception.CustomException;
import ok.backend.common.exception.ErrorCode;
import ok.backend.common.security.util.SecurityUserDetailService;
import ok.backend.friendship.domain.entity.Friendship;
import ok.backend.friendship.domain.entity.FriendshipRequest;
import ok.backend.friendship.domain.enums.FriendshipRequestStatus;
import ok.backend.friendship.domain.repository.FriendshipCustomRepositoryImpl;
import ok.backend.friendship.domain.repository.FriendshipRepository;
import ok.backend.friendship.domain.repository.FriendshipRequestRepository;
import ok.backend.friendship.dto.FriendshipRequestResponseDto;
import ok.backend.friendship.dto.FriendshipRequestUpdateDto;
import ok.backend.friendship.dto.FriendshipResponseDto;
import ok.backend.member.domain.entity.Member;
import ok.backend.member.service.MemberService;
import ok.backend.storage.service.AwsFileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class FriendshipServiceTest {
    @Mock
    private FriendshipRequestRepository friendshipRequestRepository;

    @Mock
    private FriendshipRepository friendshipRepository;

    @Mock
    private MemberService memberService;

    @Mock
    private FriendshipCustomRepositoryImpl friendshipCustomRepositoryImpl;

    @Mock
    private SecurityUserDetailService securityUserDetailService;

    @Mock
    private AwsFileService awsFileService;

    @InjectMocks
    private FriendshipService friendshipService;

    private Member member;
    private Member toMember;
    private FriendshipRequest friendshipRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        member = mock(Member.class);
        toMember = mock(Member.class);
        friendshipRequest = mock(FriendshipRequest.class);

        when(securityUserDetailService.getLoggedInMember()).thenReturn(member);
        when(memberService.findMemberById(securityUserDetailService.getLoggedInMember().getId())).thenReturn(member);

        when(member.getId()).thenReturn(1L);
        when(toMember.getId()).thenReturn(2L);

        when(friendshipRequest.getMember()).thenReturn(toMember);
        when(friendshipRequest.getMember().getBackground()).thenReturn("background url");

        when(memberService.findMemberById(member.getId())).thenReturn(member);
        when(memberService.findMemberByNickname(anyString())).thenReturn(toMember);
    }

    @Test
    @DisplayName("친구 요청 생성 - 성공")
    void createFriendshipRequest_success() {
        // given
        when(friendshipRepository.findByMemberIdAndOtherId(member.getId(), toMember.getId()))
                .thenReturn(Optional.empty());
        when(friendshipRequestRepository.findByMemberIdAndReceiverIdAndStatus(
                member.getId(), toMember.getId(), FriendshipRequestStatus.WAITING))
                .thenReturn(Optional.empty());

        // when
        friendshipService.createFriendshipRequest("nickname");

        // then
        ArgumentCaptor<FriendshipRequest> friendshipRequestCaptor = ArgumentCaptor.forClass(FriendshipRequest.class);
        verify(friendshipRequestRepository).save(friendshipRequestCaptor.capture());
        assertEquals(member, friendshipRequestCaptor.getValue().getMember());
        assertEquals(FriendshipRequestStatus.WAITING, friendshipRequestCaptor.getValue().getStatus());
    }

    @Test
    @DisplayName("친구 요청 생성 - 이미 등록된 친구인 경우")
    void createFriendshipRequest_duplicateSocial() {
        // given
        when(friendshipRepository.findByMemberIdAndOtherId(member.getId(), toMember.getId()))
                .thenReturn(Optional.of(mock(Friendship.class)));

        // when
        CustomException exception = assertThrows(CustomException.class, () ->
                friendshipService.createFriendshipRequest("nickname"));

        // then
        assertEquals(ErrorCode.DUPLICATE_SOCIAL, exception.getErrorCode());
    }

    @Test
    @DisplayName("친구 요청 조회 - 성공")
    void getFriendshipRequest_success() {
        // given
        List<FriendshipRequest> mockFriendshipRequests = List.of(friendshipRequest);
        when(friendshipCustomRepositoryImpl.findFriendshipRequestsByReceiverId(
                member.getId())).thenReturn(mockFriendshipRequests);


        // when
        List<FriendshipRequestResponseDto> friendshipRequestResponseDtos = friendshipService.getFriendshipRequest();

        // then
        assertNotNull(friendshipRequestResponseDtos);
        assertFalse(friendshipRequestResponseDtos.isEmpty());
    }

    @Test
    @DisplayName("친구 요청 수락, 거절 - 성공")
    void updateFriendshipRequest_success() {
        // given
        FriendshipRequestUpdateDto friendshipRequestUpdateDto = mock(FriendshipRequestUpdateDto.class);
        when(friendshipRequestUpdateDto.getSenderId()).thenReturn(2L);
        when(friendshipRequestUpdateDto.getIsAccept()).thenReturn(true);

        when(friendshipRequestRepository.findByMemberIdAndReceiverIdAndStatus(
                2L, member.getId(), FriendshipRequestStatus.WAITING))
                .thenReturn(Optional.of(friendshipRequest));

        when(memberService.findMemberById(2L)).thenReturn(toMember);

        // when
        friendshipService.updateFriendshipRequest(friendshipRequestUpdateDto);

        // then
        ArgumentCaptor<Friendship> friendshipCaptor = ArgumentCaptor.forClass(Friendship.class);
        verify(friendshipRepository, times(2)).save(friendshipCaptor.capture());

        List<Friendship> savedFriendships = friendshipCaptor.getAllValues();
        assertEquals(member.getId(), savedFriendships.get(0).getOtherId());
        assertEquals(toMember.getId(), savedFriendships.get(1).getOtherId());

        verify(friendshipRequest).updateStatus(FriendshipRequestStatus.ACCEPTED);
        verify(friendshipRequestRepository).save(friendshipRequest);
    }

    @Test
    @DisplayName("친구 요청 수락, 거절 - 요청이 존재하지 않음")
    void updateFriendshipRequest_friendshipRequestNotFound() {
        // given
        FriendshipRequestUpdateDto friendshipRequestUpdateDto = mock(FriendshipRequestUpdateDto.class);
        when(friendshipRequestUpdateDto.getSenderId()).thenReturn(2L);
        when(friendshipRequestRepository.findByMemberIdAndReceiverIdAndStatus(
                2L, member.getId(), FriendshipRequestStatus.WAITING))
                .thenReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class, () ->
                friendshipService.updateFriendshipRequest(friendshipRequestUpdateDto));

        // then
        assertEquals(ErrorCode.FRIENDSHIP_REQUEST_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("친구 목록 조회 - 성공")
    void getFriendshipMembers_success() throws Exception {
        // given
        List<Member> mockMembers = List.of(toMember);
        when(friendshipCustomRepositoryImpl.findMembersByMemberId(member.getId())).thenReturn(mockMembers);

        // when
        List<FriendshipResponseDto> friendshipResponseDto = friendshipService.getFriendshipMembers();

        // then
        assertNotNull(friendshipResponseDto);
        assertFalse(friendshipResponseDto.isEmpty());
    }

    @Test
    @DisplayName("친구 삭제 - 성공")
    void deleteFriendship_success() {
        // when
        friendshipService.deleteFriendship(2L);

        // then
        ArgumentCaptor<Long> longCaptor = ArgumentCaptor.forClass(Long.class);
        verify(friendshipCustomRepositoryImpl).deleteFriendshipByMemberIdAndOtherId(longCaptor.capture(), eq(2L));

        assertEquals(member.getId(), longCaptor.getValue());
    }
}