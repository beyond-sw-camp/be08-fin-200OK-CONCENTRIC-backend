package ok.backend.team.service;

import jakarta.transaction.Transactional;
import ok.backend.common.exception.CustomException;
import ok.backend.common.security.util.SecurityUser;
import ok.backend.member.domain.entity.Member;
import ok.backend.member.domain.repository.MemberRepository;
import ok.backend.team.domain.entity.Team;
import ok.backend.team.domain.entity.TeamList;
import ok.backend.team.domain.repository.TeamListRepository;
import ok.backend.team.domain.repository.TeamRepository;
import ok.backend.team.dto.TeamRequestDto;
import ok.backend.team.dto.TeamResponseDto;
import ok.backend.team.dto.TeamUpdateRequestDto;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static ok.backend.common.exception.ErrorCode.*;

@Service
@Transactional
public class TeamService {
    private final TeamRepository teamRepository;
    private final TeamListRepository teamListRepository;
    private final MemberRepository memberRepository;

    public TeamService(TeamRepository teamRepository, TeamListRepository teamListRepository, MemberRepository memberRepository) {
        this.teamRepository = teamRepository;
        this.teamListRepository = teamListRepository;
        this.memberRepository = memberRepository;
    }

    public Long getLoggedInUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        return securityUser.getMember().getId();
    }

    // 팀 목록 조회 (로그인한 사람것만 조회가능)
    public List<TeamResponseDto> getAllTeams() {
        Long currentUserId = getLoggedInUserId();

        Member currentMember = memberRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        List<TeamList> myTeamLists = teamListRepository.findByMemberId(currentMember.getId());

        return myTeamLists.stream()
                .map(teamList -> new TeamResponseDto(teamList.getTeam()))
                .collect(Collectors.toList());
    }

    // 특정 팀 조회 (로그인한 사람것만 조회가능)
    public TeamResponseDto getTeamById(Long id) {
        Long currentUserId = getLoggedInUserId();
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new CustomException(TEAM_NOT_FOUND));

        boolean isMemberOfTeam = teamListRepository.existsByTeamIdAndMemberId(team.getId(), currentUserId);

        // 팀에 속하지 않으면 예외처리
        if (!isMemberOfTeam) {
            throw new CustomException(NOT_ACCESS_TEAM);
        }
        return new TeamResponseDto(team);
    }

    // 팀 생성
    public TeamResponseDto createTeam(TeamRequestDto teamRequestDto) {
        if (teamRepository.existsByName(teamRequestDto.getName())) {
            throw new CustomException(DUPLICATE_TEAM_REQUEST);
        }

        Team team = new Team();
        team.setName(teamRequestDto.getName());
        // creatorId를 자동으로 회원 id로 설정
        team.setCreatorId(getLoggedInUserId());
        teamRepository.save(team);

        TeamList teamList = new TeamList();
        teamList.setTeam(team);

        teamList.setMember(memberRepository.findById(getLoggedInUserId())
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND)));

        teamListRepository.save(teamList);
        return new TeamResponseDto(team);
    }

    // 팀 이름 수정 (생성자만 수정 가능)
    public void updateTeam(Long id, TeamUpdateRequestDto teamUpdateRequestDTO) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new CustomException(TEAM_NOT_FOUND));

        Long currentMemberId = getLoggedInUserId();

        if (!currentMemberId.equals(team.getCreatorId())) {
            throw new CustomException(NOT_ACCESS_TEAM);
        }

        team.setName(teamUpdateRequestDTO.getName());
        teamRepository.save(team);
    }

    // 팀 삭제 (생성자만 삭제 가능)
    public void deleteTeam(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new CustomException(TEAM_NOT_FOUND));

        Long currentMemberId = getLoggedInUserId();

        if (!currentMemberId.equals(team.getCreatorId())) {
            throw new CustomException(NOT_ACCESS_TEAM);
        }

        teamRepository.delete(team);
    }
}