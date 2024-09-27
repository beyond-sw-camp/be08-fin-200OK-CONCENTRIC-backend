package ok.backend.schedule.service;

import ok.backend.schedule.domain.entity.Schedule;
import ok.backend.schedule.domain.entity.TeamSchedule;
import ok.backend.schedule.domain.repository.ScheduleRepository;
import ok.backend.schedule.domain.repository.TeamScheduleRepository;
import ok.backend.schedule.dto.req.TeamScheduleRequestDto;
import ok.backend.schedule.dto.res.TeamScheduleResponseDto;
import ok.backend.common.security.util.SecurityUser;
import ok.backend.team.domain.Team;
import ok.backend.member.domain.repository.MemberRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeamScheduleService {

    private final TeamScheduleRepository teamScheduleRepository;
    private final ScheduleRepository scheduleRepository;
    private final MemberRepository memberRepository;

    public TeamScheduleService(TeamScheduleRepository teamScheduleRepository, ScheduleRepository scheduleRepository, MemberRepository memberRepository) {
        this.teamScheduleRepository = teamScheduleRepository;
        this.scheduleRepository = scheduleRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public List<TeamScheduleResponseDto> getTeamSchedulesForLoggedInUser() {
        Long loggedInUserId = getLoggedInUserId();

        var member = memberRepository.findById(loggedInUserId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        List<Long> teamIds = member.getTeamList().stream()
                .map(team -> team.getTeam().getId())
                .collect(Collectors.toList());

        List<TeamSchedule> teamSchedules = teamScheduleRepository.findByTeam_IdIn(teamIds);
        return teamSchedules.stream().map(TeamScheduleResponseDto::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TeamScheduleResponseDto> getTeamSchedulesByTeamId(Long teamId) {
        Long loggedInUserId = getLoggedInUserId();

        // 로그인한 유저의 팀 ID 목록 가져오기
        var member = memberRepository.findById(loggedInUserId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        List<Long> teamIds = member.getTeamList().stream()
                .map(team -> team.getTeam().getId())
                .collect(Collectors.toList());

        // 유저가 속한 팀인지 확인
        if (!teamIds.contains(teamId)) {
            throw new RuntimeException("You are not authorized to view schedules for this team.");
        }

        // 해당 팀의 일정 조회
        List<TeamSchedule> teamSchedules = teamScheduleRepository.findByTeamId(teamId);
        return teamSchedules.stream().map(TeamScheduleResponseDto::new).collect(Collectors.toList());
    }

    @Transactional
    public TeamScheduleResponseDto createTeamSchedule(TeamScheduleRequestDto teamScheduleRequestDto) {
        Long teamId = teamScheduleRequestDto.getTeamId();
        Schedule schedule = scheduleRepository.findById(teamScheduleRequestDto.getScheduleId())
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        TeamSchedule teamSchedule = TeamSchedule.builder()
                .team(Team.builder().id(teamId).build())
                .schedule(schedule)
                .build();

        teamScheduleRepository.save(teamSchedule);
        return new TeamScheduleResponseDto(teamSchedule);
    }

    @Transactional
    public TeamScheduleResponseDto updateTeamSchedule(Long id, TeamScheduleRequestDto teamScheduleRequestDto) {
        Long loggedInUserId = getLoggedInUserId();
        TeamSchedule existingTeamSchedule = teamScheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team Schedule not found"));

        Schedule schedule = scheduleRepository.findById(teamScheduleRequestDto.getScheduleId())
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        TeamSchedule updatedTeamSchedule = existingTeamSchedule.toBuilder()
                .schedule(schedule)
                .build();

        teamScheduleRepository.save(updatedTeamSchedule);
        return new TeamScheduleResponseDto(updatedTeamSchedule);
    }

    @Transactional
    public void deleteTeamSchedule(Long id) {
        Long loggedInUserId = getLoggedInUserId();
        TeamSchedule existingTeamSchedule = teamScheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team Schedule not found"));

        teamScheduleRepository.deleteById(id);
    }

    private Long getLoggedInUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        return securityUser.getMember().getId();
    }
}
