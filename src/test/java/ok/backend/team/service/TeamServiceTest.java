package ok.backend.team.service;
import ok.backend.chat.domain.entity.ChatRoom;
import ok.backend.chat.service.ChatService;
import ok.backend.common.exception.CustomException;
import ok.backend.common.exception.ErrorCode;
import ok.backend.common.security.util.SecurityUserDetailService;
import ok.backend.member.domain.entity.Member;
import ok.backend.member.dto.MemberProfileResponseDto;
import ok.backend.member.service.MemberService;
import ok.backend.storage.service.*;
import ok.backend.team.domain.entity.Team;
import ok.backend.team.domain.entity.TeamList;
import ok.backend.team.domain.repository.TeamListRepository;
import ok.backend.team.domain.repository.TeamRepository;
import ok.backend.team.dto.TeamRequestDto;
import ok.backend.team.dto.TeamResponseDto;
import ok.backend.team.dto.TeamUpdateRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.Collections;
import java.io.IOException;

import static java.util.Collections.list;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamListRepository teamListRepository;

    @Mock
    private SecurityUserDetailService securityUserDetailService;

    @Mock
    private MemberService memberService;

    @Mock
    private StorageService storageService;

    @Mock
    private ChatService chatService;

    @Mock
    private AwsFileService awsFileService;

    @Mock
    private StorageFileService storageFileService;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private TeamService teamService;

    private Team team;
    private Member member;
    private Member currentMember;
    private TeamList teamList;
    private ChatRoom chatRoom;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock 객체 생성
        team = mock(Team.class);
        member = mock(Member.class);
        currentMember = mock(Member.class);
        teamList = mock(TeamList.class);
        chatRoom = mock(ChatRoom.class);

        // 공통 모킹 설정
        when(team.getId()).thenReturn(1L);
        when(team.getName()).thenReturn("Team 1");
        when(team.getCreatorId()).thenReturn(1L);
        when(team.getImageUrl()).thenReturn("current/path/to/image");
        when(teamList.getTeam()).thenReturn(team);
        when(member.getId()).thenReturn(1L);
        when(currentMember.getId()).thenReturn(1L);
        when(securityUserDetailService.getLoggedInMember()).thenReturn(currentMember);
        when(securityUserDetailService.getLoggedInMember().getId()).thenReturn(1L);
        when(teamListRepository.existsByTeamIdAndMemberId(1L, 1L)).thenReturn(true);
        when(teamListRepository.findByMemberIdAndTeamId(1L, 1L)).thenReturn(Optional.of(teamList));
        when(teamRepository.findAllByMemberId(1L)).thenReturn(Collections.singletonList(team));
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(chatService.findByTeamId(teamList.getTeam().getId())).thenReturn(chatRoom);
        when(awsFileService.getUrl(team.getImageUrl())).thenReturn("current/path/to/image");
    }

    // 팀 생성 테스트
    @Test
    @DisplayName("팀 생성 - 성공")
    void createTeam_success() {
        // given
        when(teamRepository.existsByName(anyString())).thenReturn(false);
        when(teamRepository.save(any(Team.class))).thenReturn(team);

        // when
        TeamResponseDto teamResponseDto = teamService.createTeam(new TeamRequestDto("New Team"));

        // then
        assertNotNull(teamResponseDto);
        verify(storageService).createTeamStorage(anyLong());
        verify(chatService).createTeamChat(anyLong());
        assertEquals("New Team", teamResponseDto.getName());
    }

    @Test
    @DisplayName("팀 생성 - 중복된 팀 이름")
    void createTeam_duplicate() {
        // given
        when(teamRepository.existsByName(anyString())).thenReturn(true);

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            teamService.createTeam(new TeamRequestDto("Duplicate Team"));
        });

        // then
        assertEquals(ErrorCode.DUPLICATE_TEAM_REQUEST, exception.getErrorCode());
    }

    // 팀 상세 조회 테스트
    @Test
    @DisplayName("팀 상세 조회 - 성공")
    void getTeamById_success() {

        // when
        TeamResponseDto teamResponseDto = teamService.getTeamById(1L);

        // then
        assertNotNull(teamResponseDto);
        assertEquals("Team 1", teamResponseDto.getName());
        verify(teamRepository).findById(1L);
    }

    @Test
    @DisplayName("팀 상세 조회 - 팀을 찾을 수 없음")
    void getTeamById_notFound() {
        // given
        when(teamRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            teamService.getTeamById(1L);
        });

        // then
        assertEquals(ErrorCode.TEAM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("팀 상세 조회 - 접근 권한 없음")
    void getTeamById_notAccess() {
        // given
        when(teamListRepository.existsByTeamIdAndMemberId(1L, 1L)).thenReturn(false);

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            teamService.getTeamById(1L);
        });

        // then
        assertEquals(ErrorCode.NOT_ACCESS_TEAM, exception.getErrorCode());
    }

    // 팀 목록 조회
    @Test
    @DisplayName("팀 목록 조회 - 성공")
    void getAllTeams_success() {
        // given
        Long currentMemberId = securityUserDetailService.getLoggedInMember().getId();
        when(memberService.findMemberById(currentMemberId)).thenReturn(currentMember);

        // when
        List<TeamResponseDto> teamResponseDto = teamService.getAllTeams();

        // then
        assertNotNull(teamResponseDto);
        assertEquals(1, teamResponseDto.size());
        assertEquals("Team 1", teamResponseDto.get(0).getName());
        verify(teamRepository).findAllByMemberId(anyLong());
    }

    // 팀원 조회
    @Test
    @DisplayName("팀원 조회 - 성공")
    void getTeamMembers_success() throws Exception {
        // given
        Member teamMember1 = mock(Member.class);
        List<Member> memberList = List.of(member, teamMember1);

        when(teamRepository.getTeamMembers(1L)).thenReturn(memberList);
        when(memberService.getMemberProfilesByMemberList(anyList()))
                .thenReturn(List.of(mock(MemberProfileResponseDto.class), mock(MemberProfileResponseDto.class)));

        ArgumentCaptor<List<Member>> memberListCaptor = ArgumentCaptor.forClass(List.class);

        // when
        List<MemberProfileResponseDto> memberProfileResponseDtos = teamService.getTeamMembers(1L);

        // then
        assertEquals(2, memberProfileResponseDtos.size());
        verify(teamRepository).getTeamMembers(1L);
        verify(memberService).getMemberProfilesByMemberList(memberListCaptor.capture());
    }

        // 팀 초대 수락
    @Test
    @DisplayName("팀 초대 수락 - 성공")
    void joinTeam_success() {
        // given
        Long currentMemberId = securityUserDetailService.getLoggedInMember().getId();
        when(memberService.findMemberById(currentMemberId)).thenReturn(currentMember);
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(teamListRepository.existsByTeamIdAndMemberId(1L, 1L)).thenReturn(false);

        // when
        teamService.joinTeam(1L);

        // then
        verify(teamListRepository).save(any(TeamList.class));
        verify(chatService).joinChat(anyLong());
    }

    @Test
    @DisplayName("팀 초대 수락 - 이미 가입된 팀")
    void joinTeam_duplicate() {
        // given
        Long currentMemberId = securityUserDetailService.getLoggedInMember().getId();
        when(memberService.findMemberById(currentMemberId)).thenReturn(currentMember);
        when(teamListRepository.existsByTeamIdAndMemberId(1L, 1L)).thenReturn(true);

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            teamService.joinTeam(1L);
        });

        // then
        assertEquals(ErrorCode.DUPLICATE_TEAM, exception.getErrorCode());
    }

    // 팀 정보 수정
    @Test
    @DisplayName("팀 정보 수정 - 성공")
    void updateTeam_success() throws IOException {
        // given
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(teamRepository.save(team)).thenReturn(team);
        when(storageFileService.saveTeamProfileImage(anyLong(), anyString(), any(MultipartFile.class)))
                .thenReturn("path/to/new/image");
        when(team.getName()).thenReturn("new name");

        // when
        TeamResponseDto teamResponseDto = teamService.updateTeam(1L, new TeamUpdateRequestDto("new name"), multipartFile);

        // then
        assertNotNull(teamResponseDto);
        verify(team).updateTeam(any(TeamUpdateRequestDto.class));
        verify(team).updatePath(anyString());
        assertEquals("new name", teamResponseDto.getName());
        verify(teamRepository).save(team);
    }

    @Test
    @DisplayName("팀 정보 수정 - 팀을 찾을 수 없음")
    void updateTeam_notFound() {
        // given
        when(teamRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            teamService.updateTeam(1L, new TeamUpdateRequestDto("new name"), multipartFile);
        });

        // then
        assertEquals(ErrorCode.TEAM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("팀 정보 수정 - 접근 권한 없음")
    void updateTeam_notAccess() {
        // given
        when(team.getCreatorId()).thenReturn(2L);

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            teamService.updateTeam(1L, new TeamUpdateRequestDto("new name"), multipartFile);
        });

        // then
        assertEquals(ErrorCode.NOT_ACCESS_TEAM, exception.getErrorCode());
    }

    // 팀 삭제
    @Test
    @DisplayName("팀 삭제 - 성공")
    void deleteTeam_success() {
        // when
        teamService.deleteTeam(1L);

        // then
        verify(storageService).deleteTeamStorage(1L);
        verify(chatService).deleteChat(1L);
        verify(teamRepository).delete(team);
    }

    @Test
    @DisplayName("팀 삭제 - 팀을 찾을 수 없음")
    void deleteTeam_notFound() {
        // given
        when(teamRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            teamService.deleteTeam(1L);
        });

        // then
        assertEquals(ErrorCode.TEAM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("팀 삭제 - 접근 권한 없음")
    void deleteTeam_notAccess() {
        // given
        when(team.getCreatorId()).thenReturn(2L);

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            teamService.deleteTeam(1L);
        });

        // then
        assertEquals(ErrorCode.NOT_ACCESS_TEAM, exception.getErrorCode());
    }

    // 팀 나가기
    @Test
    @DisplayName("팀 나가기 - 성공")
    void leaveTeam_success() {
        // given
        Long currentMemberId = securityUserDetailService.getLoggedInMember().getId();
        when(chatService.findByTeamId(teamList.getTeam().getId())).thenReturn(mock(ChatRoom.class));
        when(teamListRepository.findByMemberIdAndTeamId(currentMemberId, 1L)).thenReturn(Optional.ofNullable(teamList));

        // when
        teamService.leaveTeam(1L);

        // then
        verify(teamListRepository).delete(teamList);
        verify(chatService).dropChat(anyLong());
    }

    @Test
    @DisplayName("팀 나가기 - 접근 권한 없음")
    void leaveTeam_notAccess() {
        // given
        when(teamListRepository.findByMemberIdAndTeamId(1L, 1L)).thenReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            teamService.leaveTeam(1L);
        });

        // then
        assertEquals(ErrorCode.NOT_ACCESS_TEAM, exception.getErrorCode());
    }

    // 팀원 강퇴
    @Test
    @DisplayName("팀원 강퇴 - 성공")
    void removeTeamMember_success() {
        // given
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(teamListRepository.findByMemberIdAndTeamId(2L, 1L)).thenReturn(Optional.of(teamList));

        // when
        teamService.removeTeamMember(1L, 2L);

        // then
        verify(teamListRepository).delete(teamList);
        verify(teamRepository).findById(1L);
    }

    @Test
    @DisplayName("팀원 강퇴 - 팀을 찾을 수 없음")
    void removeTeamMember_teamNotFound() {
        // given
        when(teamRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            teamService.removeTeamMember(1L, 2L);
        });

        // then
        assertEquals(ErrorCode.TEAM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("팀원 강퇴 - 접근 권한 없음")
    void removeTeamMember_notAccess() {
        // given
        when(team.getCreatorId()).thenReturn(2L); // 다른 사용자가 만든 팀

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            teamService.removeTeamMember(1L, 2L);
        });

        // then
        assertEquals(ErrorCode.NOT_ACCESS_TEAM, exception.getErrorCode());
    }

    @Test
    @DisplayName("팀원 강퇴 - 팀원을 찾을 수 없음")
    void removeTeamMember_memberNotFound() {
        // given
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(teamListRepository.findByMemberIdAndTeamId(2L, 1L)).thenReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            teamService.removeTeamMember(1L, 2L);
        });

        // then
        assertEquals(ErrorCode.MEMBER_NOT_FOUND, exception.getErrorCode());
    }
}
