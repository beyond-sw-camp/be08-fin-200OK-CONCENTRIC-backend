package ok.backend.schedule.service;

import ok.backend.schedule.dto.res.TeamScheduleResponseDto;
import ok.backend.schedule.domain.entity.Schedule;
import ok.backend.schedule.domain.entity.TeamSchedule;
import ok.backend.schedule.domain.repository.ScheduleRepository;
import ok.backend.schedule.domain.repository.TeamScheduleRepository;
import ok.backend.schedule.dto.req.TeamScheduleRequestDto;
import ok.backend.common.security.util.SecurityUser;
import ok.backend.member.domain.repository.MemberRepository;
import ok.backend.team.domain.entity.Team;
import ok.backend.common.exception.CustomException;
import ok.backend.common.exception.ErrorCode;
import ok.backend.team.domain.repository.TeamRepository; // 팀 레포지토리 추가
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
    private final TeamRepository teamRepository; // 팀 레포지토리 추가

    public TeamScheduleService(TeamScheduleRepository teamScheduleRepository,
                               ScheduleRepository scheduleRepository,
                               MemberRepository memberRepository,
                               TeamRepository teamRepository) {
        this.teamScheduleRepository = teamScheduleRepository;
        this.scheduleRepository = scheduleRepository;
        this.memberRepository = memberRepository;
        this.teamRepository = teamRepository;
    }

    // 팀 일정 조회
    @Transactional(readOnly = true)
    public List<TeamScheduleResponseDto> getTeamSchedulesForLoggedInUser() {
        SecurityUser securityUser = getLoggedInUser();

        var member = memberRepository.findById(securityUser.getMember().getId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        List<Long> teamIds = member.getTeamList().stream()
                .map(team -> team.getTeam().getId())
                .collect(Collectors.toList());

        List<TeamSchedule> teamSchedules = teamScheduleRepository.findByTeamIdIn(teamIds);
        return teamSchedules.stream().map(TeamScheduleResponseDto::new).collect(Collectors.toList());
    }

    // 특정 팀 일정 조회
    @Transactional(readOnly = true)
    public List<TeamScheduleResponseDto> getTeamSchedulesByTeamId(Long teamId) {
        SecurityUser securityUser = getLoggedInUser();

        var member = memberRepository.findById(securityUser.getMember().getId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        List<Long> teamIds = member.getTeamList().stream()
                .map(team -> team.getTeam().getId())
                .collect(Collectors.toList());

        if (!teamIds.contains(teamId)) {
            throw new CustomException(ErrorCode.NOT_ACCESS_TEAM);
        }

        List<TeamSchedule> teamSchedules = teamScheduleRepository.findByTeamId(teamId);
        return teamSchedules.stream().map(TeamScheduleResponseDto::new).collect(Collectors.toList());
    }

    // 팀 일정 생성
    @Transactional
    public TeamScheduleResponseDto createTeamSchedule(TeamScheduleRequestDto teamScheduleRequestDto) {
        Schedule schedule = scheduleRepository.findById(teamScheduleRequestDto.getScheduleId())
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));

        Team team = teamRepository.findById(teamScheduleRequestDto.getTeamId())
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));

        TeamSchedule teamSchedule = TeamSchedule.builder()
                .team(team)
                .schedule(schedule)
                .build();

        teamScheduleRepository.save(teamSchedule);
        return new TeamScheduleResponseDto(teamSchedule);
    }

    // 팀 일정 수정
    @Transactional
    public TeamScheduleResponseDto updateTeamSchedule(Long id, TeamScheduleRequestDto teamScheduleRequestDto) {
        SecurityUser securityUser = getLoggedInUser();

        TeamSchedule existingTeamSchedule = teamScheduleRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));

        Schedule schedule = scheduleRepository.findById(teamScheduleRequestDto.getScheduleId())
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));

        TeamSchedule updatedTeamSchedule = existingTeamSchedule.toBuilder()
                .schedule(schedule)
                .build();

        teamScheduleRepository.save(updatedTeamSchedule);
        return new TeamScheduleResponseDto(updatedTeamSchedule);
    }

    // 팀 일정 삭제
    @Transactional
    public void deleteTeamSchedule(Long id) {
        SecurityUser securityUser = getLoggedInUser();

        TeamSchedule existingTeamSchedule = teamScheduleRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));

        teamScheduleRepository.deleteById(id);
    }

    private SecurityUser getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (SecurityUser) authentication.getPrincipal();
    }
}
