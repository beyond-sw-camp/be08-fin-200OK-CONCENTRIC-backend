package ok.backend.chat.service;

import ok.backend.chat.domain.entity.ChatRoom;
import ok.backend.chat.domain.entity.ChatRoomList;
import ok.backend.chat.domain.entity.Type;
import ok.backend.chat.domain.repository.ChatRoomListRepository;
import ok.backend.chat.domain.repository.ChatRoomRepository;
import ok.backend.chat.dto.req.ChatRoomListRequestDto;
import ok.backend.chat.dto.req.ChatRoomRequestDto;
import ok.backend.chat.dto.res.ChatRoomListResponseDto;
import ok.backend.chat.dto.res.ChatRoomMemberResponseDto;
import ok.backend.common.exception.CustomException;
import ok.backend.common.security.util.SecurityUser;
import ok.backend.common.security.util.SecurityUserDetailService;
import ok.backend.member.domain.entity.Member;
import ok.backend.member.service.MemberService;
import ok.backend.storage.service.StorageService;
import ok.backend.team.domain.entity.Team;
import ok.backend.team.domain.entity.TeamList;
import ok.backend.team.service.TeamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static ok.backend.common.exception.ErrorCode.*;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ChatServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatRoomListRepository chatRoomListRepository;

    @Mock
    private SecurityUserDetailService securityUserDetailService;

    @Mock
    private MemberService memberService;

    @Mock
    private TeamService teamService;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private ChatService chatService;

    private Member existedMember;
    private Member existedFriend;
    private ChatRoom existedChatRoom;
    private ChatRoomList existedChatRoomListMember;
    private ChatRoomList existedChatRoomListFriend;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Members
        existedMember = Mockito.mock(Member.class);
        existedFriend = Mockito.mock(Member.class);

        Mockito.when(existedMember.getId()).thenReturn(1L);
        Mockito.when(existedFriend.getId()).thenReturn(2L);
        Mockito.when(existedMember.getNickname()).thenReturn("member nickname");
        Mockito.when(existedFriend.getNickname()).thenReturn("friend nickname");


        // Chat
        existedChatRoom = Mockito.mock(ChatRoom.class);
        existedChatRoomListMember = Mockito.mock(ChatRoomList.class);
        existedChatRoomListFriend = Mockito.mock(ChatRoomList.class);

        Mockito.when(existedChatRoomListMember.getChatRoom()).thenReturn(existedChatRoom);
        Mockito.when(existedChatRoomListFriend.getChatRoom()).thenReturn(existedChatRoom);
        Mockito.when(existedChatRoomListMember.getMember()).thenReturn(existedMember);
        Mockito.when(existedChatRoomListFriend.getMember()).thenReturn(existedFriend);
        Mockito.when(existedChatRoom.getId()).thenReturn(1L);
        Mockito.when(existedChatRoom.getName()).thenReturn("chat room");
        Mockito.when(existedChatRoom.getType()).thenReturn(Type.P);
        Mockito.when(existedChatRoom.getChatRoomList()).thenReturn(Arrays.asList(existedChatRoomListMember, existedChatRoomListFriend));

        // Security
        SecurityUser securityUser = Mockito.mock(SecurityUser.class);
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(securityUser.getMember()).thenReturn(existedMember);
        Mockito.when(authentication.getPrincipal()).thenReturn(securityUser);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);

        // Service
        Mockito.when(securityUserDetailService.getLoggedInMember()).thenReturn(existedMember);
        Mockito.when(memberService.findMemberById(1L)).thenReturn(existedMember);
        Mockito.when(memberService.findMemberById(2L)).thenReturn(existedFriend);

        // Repository
        Mockito.when(chatRoomListRepository.findByMemberId(1L)).thenReturn(List.of(existedChatRoomListMember, existedChatRoomListFriend));
        Mockito.when(chatRoomListRepository.findByMemberIdAndChatRoomId(securityUserDetailService.getLoggedInMember().getId(),1L))
                .thenReturn(Optional.of(existedChatRoomListMember));
        Mockito.when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(existedChatRoom));
        Mockito.when(chatRoomRepository.findByTeamId(1L)).thenReturn(Optional.of(existedChatRoom));
//        Mockito.when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(existedChatRoom);
    }

    @Test
    @DisplayName("개인 채팅방 생성 - 성공")
    void createChat_success() {
        // given
        ChatRoomRequestDto chatRoomRequestDto = Mockito.mock(ChatRoomRequestDto.class);
        Mockito.when(chatRoomRequestDto.getName()).thenReturn("new private chat name");

        ArgumentCaptor<ChatRoom> chatRoomCaptor = ArgumentCaptor.forClass(ChatRoom.class);
        Mockito.when(chatRoomRepository.save(chatRoomCaptor.capture())).thenAnswer(invocation -> {
            return chatRoomCaptor.getValue();
        });

        // when
        chatService.createChat(3L, chatRoomRequestDto);

        // then
        verify(chatRoomRepository, times(1)).save(chatRoomCaptor.capture());
        ChatRoom capturedChatRoom = chatRoomCaptor.getValue();
        assertNotNull(capturedChatRoom);
        assertEquals("new private chat name", capturedChatRoom.getName());
        verify(storageService, times(1)).createChatStorage(capturedChatRoom.getId());
    }

    @Test
    @DisplayName("개인 채팅방 생성 - 유효하지 않음")
    void createChat_invalid() {
        // given
        ChatRoomRequestDto chatRoomRequestDto = new ChatRoomRequestDto("new private chat name");

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            chatService.createChat(1L, chatRoomRequestDto);
                });

        // then
        assertEquals(INVALID_CHAT_REQUEST, exception.getErrorCode());
    }

    @Test
    @DisplayName("개인 채팅방 생성 - 중복 채팅방 참여")
    void createChat_duplicate() {
        // given
        ChatRoomRequestDto chatRoomRequestDto = new ChatRoomRequestDto("new private chat name");

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            chatService.createChat(2L, chatRoomRequestDto);
                });

        //then
        assertEquals(DUPLICATE_CHAT, exception.getErrorCode());
    }

    @Test
    @DisplayName("팀 채팅방 생성 - 성공")
    void createTeamChat_success() {
        // given
        Team existedTeam = Mockito.mock(Team.class);
        Mockito.when(existedTeam.getName()).thenReturn("new team chat name");
        TeamList existedTeamListMember = Mockito.mock(TeamList.class);
        TeamList existedTeamListTeamMember = Mockito.mock(TeamList.class);
        List<TeamList> teamMembers = List.of(existedTeamListMember, existedTeamListTeamMember);

        ArgumentCaptor<ChatRoom> chatRoomCaptor = ArgumentCaptor.forClass(ChatRoom.class);

        // when
        Mockito.when(teamService.findById(1L)).thenReturn(existedTeam);
        Mockito.when(teamService.findByTeamId(1L)).thenReturn(List.of(existedTeamListMember, existedTeamListTeamMember));


        Mockito.when(chatRoomRepository.save(chatRoomCaptor.capture())).thenAnswer(invocation -> {
            return chatRoomCaptor.getValue();
        });

        chatService.createTeamChat(1L);

        // then
        verify(chatRoomRepository, times(1)).save(chatRoomCaptor.capture());
        ChatRoom capturedChatRoom = chatRoomCaptor.getValue();
        assertNotNull(capturedChatRoom);
        assertEquals("new team chat name", capturedChatRoom.getName());
        verify(chatRoomListRepository, times(teamMembers.size())).save(any(ChatRoomList.class));
        verify(storageService, times(1)).createChatStorage(capturedChatRoom.getId());
    }

    @Test
    @DisplayName("팀 채팅방 참여 - 성공")
    void joinChat_success() {
        // given
        ArgumentCaptor<ChatRoomList> chatRoomListCaptor = ArgumentCaptor.forClass(ChatRoomList.class);

        // when
        chatService.joinChat(1L);

        // then
        verify(chatRoomListRepository, times(1)).save(chatRoomListCaptor.capture());
        ChatRoomList capturedChatRoomList = chatRoomListCaptor.getValue();
        assertNotNull(capturedChatRoomList);
    }

    @Test
    @DisplayName("팀 채팅방 참여 - 채팅방 없음")
    void joinChat_chatRoomNotFound() {
        // given
        Mockito.when(chatRoomRepository.findByTeamId(1L)).thenReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            chatService.joinChat(1L);
        });

        // then
        assertEquals(CHAT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("채팅방 나가기 - 성공")
    void dropChat_success() {
        // given
        ArgumentCaptor<ChatRoomList> chatRoomListCaptor = ArgumentCaptor.forClass(ChatRoomList.class);
        ArgumentCaptor<ChatRoom> chatRoomCaptor = ArgumentCaptor.forClass(ChatRoom.class);

        // when
        chatService.dropChat(1L);

        // then
        verify(chatRoomListRepository, times(1)).delete(chatRoomListCaptor.capture());
        verify(chatRoomRepository, times(1)).save(chatRoomCaptor.capture());
        ChatRoomList capturedChatRoomList = chatRoomListCaptor.getValue();
        ChatRoom capturedChatRoom = chatRoomCaptor.getValue();
        assertNotNull(capturedChatRoomList);
        assertNotNull(capturedChatRoom);
    }

    @Test
    @DisplayName("채팅방 나가기 - 접근 권한 없음")
    void dropChat_notAccess() {
        // given
        Mockito.when(chatRoomListRepository.findByMemberIdAndChatRoomId(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            chatService.dropChat(1L);
        });

        // then
        assertEquals(NOT_ACCESS_CHAT, exception.getErrorCode());
    }

    @Test
    @DisplayName("채팅방 나가기 - 채팅방 없음")
    void dropChat_chatRoomNotFound() {
        // given
        Mockito.when(chatRoomRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
                    chatService.dropChat(1L);
                });

        // then
        assertEquals(CHAT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("채팅방 나가기 - 유효하지 않음")
    void dropChat_invalid() {
        // given=
        Mockito.when(securityUserDetailService.getLoggedInMember().getId()).thenReturn(1L);

        ChatRoom chatRoom = Mockito.mock(ChatRoom.class);
        Mockito.when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(chatRoom));
        Mockito.when(chatRoom.getType()).thenReturn(Type.T);
        Mockito.when(chatRoom.getTeamId()).thenReturn(1L);

        Team team = Mockito.mock(Team.class);
        Mockito.when(team.getCreatorId()).thenReturn(1L);
        Mockito.when(teamService.findById(1L)).thenReturn(team);

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            chatService.dropChat(1L);
        });

        // then
        assertEquals(INVALID_DELETE_REQUEST, exception.getErrorCode());
    }

    @Test
    @DisplayName("채팅방 삭제 - 성공")
    void deleteChat_success() {
        // given
        Team team = Mockito.mock(Team.class);
        Mockito.when(team.getCreatorId()).thenReturn(1L);
        Mockito.when(teamService.findById(1L)).thenReturn(team);
        ArgumentCaptor<ChatRoom> chatRoomCaptor = ArgumentCaptor.forClass(ChatRoom.class);

        // when
        chatService.deleteChat(1L);

        // then
        verify(chatRoomRepository, times(1)).delete(chatRoomCaptor.capture());
        ChatRoom capturedChatRoom = chatRoomCaptor.getValue();
        verify(storageService, times(1)).deleteChatStorage(capturedChatRoom.getId());
        assertNotNull(capturedChatRoom);
    }

    @Test
    @DisplayName("채팅방 삭제 - 채팅방 없음")
    void deleteChat_chatRoomNotFound() {
        // given
        Mockito.when(chatRoomRepository.findByTeamId(anyLong())).thenReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            chatService.deleteChat(1L);
        });

        // then
        assertEquals(CHAT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("채팅방 삭제 - 접근 권한 없음")
    void deleteChat_notAccess() {
        // given
        Team team = Mockito.mock(Team.class);
        Mockito.when(team.getCreatorId()).thenReturn(2L);
        Mockito.when(teamService.findById(1L)).thenReturn(team);

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            chatService.deleteChat(1L);
        });

        // then
        assertEquals(NOT_ACCESS_CHAT, exception.getErrorCode());
    }

    @Test
    @DisplayName("채팅방 이름 수정 - 성공")
    void renameChat_success() {
        // given
        ChatRoomListRequestDto chatRoomListRequestDto = Mockito.mock(ChatRoomListRequestDto.class);
        Mockito.when(chatRoomListRequestDto.getChatRoomId()).thenReturn(1L);
        Mockito.when(chatRoomListRequestDto.getNickname()).thenReturn("new chat name");

        // when
        chatService.renameChat(chatRoomListRequestDto);

        // then
        verify(existedChatRoomListMember, times(1)).updateNickname(chatRoomListRequestDto);
        verify(chatRoomListRepository, times(1)).save(existedChatRoomListMember);
    }

    @Test
    @DisplayName("채팅방 이름 수정 - 공백 입력")
    void renameChat_emptyInput() {
        // given
        ChatRoomListRequestDto chatRoomListRequestDto = Mockito.mock(ChatRoomListRequestDto.class);
        Mockito.when(chatRoomListRequestDto.getNickname()).thenReturn("");

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            chatService.renameChat(chatRoomListRequestDto);
        });

        // then
        assertEquals(EMPTY_INPUT_CHAT, exception.getErrorCode());
    }

    @Test
    @DisplayName("채팅방 이름 수정 - 접근 권한 없음")
    void renameChat_notAccess() {
        // given
        ChatRoomListRequestDto chatRoomListRequestDto = Mockito.mock(ChatRoomListRequestDto.class);
        Mockito.when(chatRoomListRequestDto.getNickname()).thenReturn("new chat name");

        Mockito.when(chatRoomListRepository.findByMemberIdAndChatRoomId(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            chatService.renameChat(chatRoomListRequestDto);
        });

        // then
        assertEquals(NOT_ACCESS_CHAT, exception.getErrorCode());
    }

    @Test
    @DisplayName("채팅방 즐겨찾기 설정 - 성공")
    void bookmarkChat_success() {
        // given
        Mockito.when(existedChatRoomListMember.getBookmark()).thenReturn(false);

        // when
        chatService.bookmarkChat(1L);

        // then
        verify(existedChatRoomListMember, times(1)).updateBookmark(true);
        verify(chatRoomListRepository, times(1)).save(existedChatRoomListMember);
    }

    @Test
    @DisplayName("채팅방 즐겨찾기 설정 - 접근 권한 없음")
    void bookmarkChat_notAccess() {
        // given
        Mockito.when(chatRoomListRepository.findByMemberIdAndChatRoomId(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            chatService.bookmarkChat(1L);
        });

        // then
        assertEquals(NOT_ACCESS_CHAT, exception.getErrorCode());
    }

    @Test
    @DisplayName("채팅방 참여자 조회 - 성공")
    void findChatParticipant_success() {
        // given
        Mockito.when(chatRoomListRepository.findByChatRoomId(1L))
                .thenReturn(Arrays.asList(existedChatRoomListMember, existedChatRoomListFriend));

        // when
        List<ChatRoomMemberResponseDto> participants = chatService.findChatParticipant(1L);

        // then
        assertEquals(2, participants.size());
        assertEquals("member nickname", participants.get(0).getNickname());
        assertEquals("friend nickname", participants.get(1).getNickname());
    }

    @Test
    @DisplayName("채팅방 참여자 조회 - 실패 (접근 권한 없음)")
    void findChatParticipant_notAccess() {
        // given
        Mockito.when(chatRoomListRepository.findByMemberIdAndChatRoomId(existedMember.getId(), 2L))
                .thenReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            chatService.findChatParticipant(2L);
        });

        // then
        assertEquals(NOT_ACCESS_CHAT, exception.getErrorCode());
    }

    @Test
    @DisplayName("채팅방 목록 조회 - 성공")
    void findChatRooms_success() {
        // given
        Mockito.when(chatRoomListRepository.findByMemberIdAndChatRoomIsActiveTrue(existedMember.getId()))
                .thenReturn(Arrays.asList(existedChatRoomListMember));

        Mockito.when(existedChatRoom.getType()).thenReturn(Type.T);
        Team team = Mockito.mock(Team.class);
        Mockito.when(team.getName()).thenReturn("team name");
        Mockito.when(team.getImageUrl()).thenReturn("team_image_url");
        Mockito.when(teamService.findById(existedChatRoom.getTeamId())).thenReturn(team);

        // when
        List<ChatRoomListResponseDto> chatRooms = chatService.findChatRooms();

        // then
        assertEquals(1, chatRooms.size());
        assertEquals("team name", chatRooms.get(0).getUserNickname());
        assertEquals("team_image_url", chatRooms.get(0).getProfileImageUrl());
    }

    @Test
    @DisplayName("채팅방 목록 조회 - 실패 (참여중인 채팅방 없음)")
    void findChatRooms_chatRoomNotFound() {
        // given
        Mockito.when(chatRoomListRepository.findByMemberId(existedMember.getId()))
                .thenReturn(Collections.emptyList());

        // when
        List<ChatRoomListResponseDto> chatRooms = chatService.findChatRooms();

        // then
        assertEquals(0, chatRooms.size());
    }
}