package ok.backend.team.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import ok.backend.common.security.util.SecurityUser;
import ok.backend.team.domain.entity.Team;
import ok.backend.team.domain.repository.TeamRepository;
import ok.backend.team.dto.TeamRequestDto;
import ok.backend.team.dto.TeamResponseDto;
import ok.backend.team.dto.TeamUpdateRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TeamService {
    private final TeamRepository teamRepository;

    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }


    // 모든 팀 조회
    public List<TeamResponseDto> getAllTeams(int page, int size) {
        Page<Team> teamPage = teamRepository.findAll(PageRequest.of(page, size));
        return teamPage.getContent().stream()
                .map(TeamResponseDto::new)
                .collect(Collectors.toList());
    }

    // 특정 팀 조회
    public TeamResponseDto getTeamById(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당하는 팀이 없습니다."));
        return new TeamResponseDto(team);
    }

    // 팀 생성
    public TeamResponseDto createTeam(TeamRequestDto teamRequestDto) {
        Team team = new Team();
        team.setName(teamRequestDto.getName());
        team.setCreatorId(teamRequestDto.getCreatorId());
        teamRepository.save(team);
        return new TeamResponseDto(team);
    }

    // 팀 이름 수정
    public void updateTeam(Long id, TeamUpdateRequestDto teamUpdateRequestDTO) {

        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당하는 팀이 없습니다."));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        Long currentMemberId = securityUser.getMember().getId();

        if (!currentMemberId.equals(team.getCreatorId())) {
            throw new SecurityException("팀명을 수정할 권한이 없습니다.");
        }

        team.setName(teamUpdateRequestDTO.getName());
        teamRepository.save(team);
    }

    // 팀 삭제
    public void deleteTeam(Long id) {
        // 팀을 찾고 없으면 예외 발생
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당하는 팀이 없습니다."));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        Long currentMemberId = securityUser.getMember().getId();

        if (!currentMemberId.equals(team.getCreatorId())) {
            throw new SecurityException("팀 정보를 삭제할 권한이 없습니다.");
        }

        teamRepository.delete(team);
    }

}
