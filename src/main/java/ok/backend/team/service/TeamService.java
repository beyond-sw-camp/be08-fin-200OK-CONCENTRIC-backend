package ok.backend.team.service;

import jakarta.transaction.Transactional;
import ok.backend.chat.domain.entity.ChatRoom;
import ok.backend.chat.service.ChatService;
import ok.backend.common.exception.CustomException;
import ok.backend.common.exception.ErrorCode;
import ok.backend.common.security.util.SecurityUserDetailService;
import ok.backend.member.domain.entity.Member;
import ok.backend.member.dto.MemberProfileResponseDto;
import ok.backend.member.dto.MemberResponseDto;
import ok.backend.member.dto.MemberUpdateRequestDto;
import ok.backend.member.service.MemberService;
import ok.backend.storage.service.AwsFileService;
import ok.backend.storage.service.StorageFileService;
import ok.backend.storage.service.StorageService;
import ok.backend.team.domain.entity.Team;
import ok.backend.team.domain.entity.TeamList;
import ok.backend.team.domain.repository.TeamListRepository;
import ok.backend.team.domain.repository.TeamRepository;
import ok.backend.team.dto.TeamMemberResponseDto;
import ok.backend.team.dto.TeamRequestDto;
import ok.backend.team.dto.TeamResponseDto;
import ok.backend.team.dto.TeamUpdateRequestDto;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ok.backend.common.exception.ErrorCode.*;

@Service
@Transactional
public class TeamService {
    private final TeamRepository teamRepository;
    private final TeamListRepository teamListRepository;
    private final SecurityUserDetailService securityUserDetailService;
    private final MemberService memberService;
    private final StorageService storageService;
    private final ChatService chatService;
    private final StorageFileService storageFileService;
    private final AwsFileService awsFileService;

    public TeamService(TeamRepository teamRepository, TeamListRepository teamListRepository, SecurityUserDetailService securityUserDetailService,
                       MemberService memberService, StorageService storageService, @Lazy ChatService chatService,
                       StorageFileService storageFileService, AwsFileService awsFileService) {
        this.teamRepository = teamRepository;
        this.teamListRepository = teamListRepository;
        this.securityUserDetailService = securityUserDetailService;
        this.memberService = memberService;
        this.storageService = storageService;
        this.chatService = chatService;
        this.storageFileService = storageFileService;
        this.awsFileService = awsFileService;
    }

    // 팀 목록 조회 (로그인한 사람것만 조회가능)
    public List<TeamResponseDto> getAllTeams() {
        Long currentMemberId = securityUserDetailService.getLoggedInMember().getId();
        Member currentMember = memberService.findMemberById(currentMemberId);

//        List<TeamList> myTeamLists = teamListRepository.findByMemberId(currentMember.getId());
//
//        return myTeamLists.stream()
//                .map(teamList -> new TeamResponseDto(teamList.getTeam()))
//                .collect(Collectors.toList());

        List<Team> teams = teamRepository.findAllByMemberId(currentMember.getId());

        return teams.stream()
                .map(TeamResponseDto::new)
                .collect(Collectors.toList());
    }

    // 특정 팀 조회 (로그인한 사람것만 조회가능)
    public TeamResponseDto getTeamById(Long id) {
        Long currentUserId = securityUserDetailService.getLoggedInMember().getId();
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new CustomException(TEAM_NOT_FOUND));

        boolean isMemberOfTeam = teamListRepository.existsByTeamIdAndMemberId(team.getId(), currentUserId);

        // 팀에 속하지 않으면 예외처리
        if (!isMemberOfTeam) {
            throw new CustomException(NOT_ACCESS_TEAM);
        }

        return this.convertToDto(team);
    }

    // 팀 생성
    public TeamResponseDto createTeam(TeamRequestDto teamRequestDto) {
        if (teamRepository.existsByName(teamRequestDto.getName())) {
            throw new CustomException(DUPLICATE_TEAM_REQUEST);
        }

        Long currentMemberId = securityUserDetailService.getLoggedInMember().getId();

        Team team = Team.builder()
                .name(teamRequestDto.getName())
                .creatorId(currentMemberId)
                .build();

        Team savedteam = teamRepository.save(team);

        storageService.createTeamStorage(savedteam.getId());

        TeamList teamList = TeamList.builder()
                .team(team)
                .member(memberService.findMemberById(currentMemberId))
                .build();

        teamListRepository.save(teamList);


        chatService.createTeamChat(savedteam.getId());

        return new TeamResponseDto(team);
    }

//    // 팀 이름 수정 (생성자만 수정 가능)
//    public void updateTeam(Long id, TeamUpdateRequestDto teamUpdateRequestDTO) {
//        Team team = teamRepository.findById(id)
//                .orElseThrow(() -> new CustomException(TEAM_NOT_FOUND));
//
//        Long currentMemberId = securityUserDetailService.getLoggedInMember().getId();
//
//        if (!currentMemberId.equals(team.getCreatorId())) {
//            throw new CustomException(NOT_ACCESS_TEAM);
//        }
//
//        team.updateName(teamUpdateRequestDTO);
//        teamRepository.save(team);
//    }

    // 팀 삭제 (생성자만 삭제 가능)
    public void deleteTeam(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new CustomException(TEAM_NOT_FOUND));

        Long currentMemberId = securityUserDetailService.getLoggedInMember().getId();

        if (!currentMemberId.equals(team.getCreatorId())) {
            throw new CustomException(NOT_ACCESS_TEAM);
        }

        storageService.deleteTeamStorage(team.getId());
        chatService.deleteChat(team.getId());

        teamRepository.delete(team);
    }

    // 팀 가입
    public void joinTeam(Long teamId) {
        Long currentMemberId = securityUserDetailService.getLoggedInMember().getId();
        Member member = memberService.findMemberById(currentMemberId);

        Team team = teamRepository.findById(teamId).orElseThrow(() -> new CustomException(TEAM_NOT_FOUND));

        boolean isMemberOfTeam = teamListRepository.existsByTeamIdAndMemberId(team.getId(), member.getId());
        if (isMemberOfTeam) {
            throw new CustomException(DUPLICATE_TEAM);
        }

        TeamList teamList = TeamList.builder()
                .team(team)
                .member(member)
                .build();

        teamListRepository.save(teamList);
        chatService.joinChat(teamId);
    }

    // 팀 나가기 (팀 리스트에서 삭제됨)
    public void leaveTeam(Long id) {
        Long currentMemberId = securityUserDetailService.getLoggedInMember().getId();

        TeamList teamList = teamListRepository.findByMemberIdAndTeamId(currentMemberId, id)
                .orElseThrow(() -> new CustomException(NOT_ACCESS_TEAM));

        ChatRoom chatRoom = chatService.findByTeamId(teamList.getTeam().getId());
        chatService.dropChat(chatRoom.getId());

        teamListRepository.delete(teamList);
    }

    // 팀원 강퇴
    public void removeTeamMember(Long teamId, Long memberIdToRemove) {
        Long currentMemberId = securityUserDetailService.getLoggedInMember().getId();

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new CustomException(TEAM_NOT_FOUND));

        if (!currentMemberId.equals(team.getCreatorId())) {
            throw new CustomException(NOT_ACCESS_TEAM);
        }
        TeamList teamList = teamListRepository.findByMemberIdAndTeamId(memberIdToRemove, teamId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        ChatRoom chatRoom = chatService.findByTeamId(teamList.getTeam().getId());
        chatService.dropTeamChat(memberIdToRemove, chatRoom.getId());

        teamListRepository.delete(teamList);

    }

    public TeamResponseDto updateTeam(Long teamId, TeamUpdateRequestDto teamUpdateRequestDto, MultipartFile file) throws IOException {
        // 로그인한 사용자 정보 가져오기
        Long currentMemberId = securityUserDetailService.getLoggedInMember().getId();

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new CustomException(TEAM_NOT_FOUND));

        if (!currentMemberId.equals(team.getCreatorId())) {
            throw new CustomException(NOT_ACCESS_TEAM); // 권한이 없으면 예외 발생
        }

        // 팀 정보 업데이트 (팀 이름 등)
        team.updateTeam(teamUpdateRequestDto);

        // 파일이 null이 아니면 파일 저장 및 경로 업데이트
        if (file != null) {
            String path = storageFileService.saveTeamProfileImage(team.getId(), team.getImageUrl(), file);
            team.updatePath(path); // 이미지 경로 업데이트
        }

        // 변경된 팀 정보 저장 후 반환
        return this.convertToDto(teamRepository.save(team));
    }


    public Team findById(Long id) {
        return teamRepository.findById(id).orElseThrow(() ->
                new CustomException(ErrorCode.TEAM_NOT_FOUND));
    }

    public List<TeamList> findByMemberId(Long memberId) {
        return teamListRepository.findByMemberId(memberId);
    }

    public List<TeamList> findByTeamId(Long teamId) {
        return teamListRepository.findByTeamId(teamId);
    }

    public List<MemberProfileResponseDto> getTeamMembers(Long teamId) throws MalformedURLException {
        List<Member> memberList = teamRepository.getTeamMembers(teamId);
        return memberService.getMemberProfilesByMemberList(memberList);
    }

    public TeamResponseDto convertToDto(Team team) {
        String imageUrl = null;

        if(team.getImageUrl() != null) {
            imageUrl = awsFileService.getUrl(team.getImageUrl());
        }

        return TeamResponseDto.builder()
                .id(team.getId())
                .name(team.getName())
                .creatorId(team.getCreatorId())
                .imageUrl(imageUrl)
                .createAt(team.getCreateAt())
                .build();
    }
}
