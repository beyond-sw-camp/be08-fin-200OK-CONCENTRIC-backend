package ok.backend.schedule.service;

import ok.backend.common.exception.CustomException;
import ok.backend.common.exception.ErrorCode;
import ok.backend.common.security.util.SecurityUserDetailService;
import ok.backend.member.service.MemberService;
import ok.backend.schedule.domain.entity.TeamSchedule;
import ok.backend.schedule.domain.repository.TeamScheduleRepository;
import ok.backend.schedule.dto.req.TeamScheduleRequestDto;
import ok.backend.schedule.dto.res.TeamScheduleResponseDto;
import ok.backend.team.service.TeamService;
import ok.backend.schedule.service.ScheduleService;
import org.hibernate.Hibernate;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeamScheduleService {

    private final TeamScheduleRepository teamScheduleRepository;
    private final ScheduleService scheduleService;
    private final TeamService teamService;
    private final SecurityUserDetailService securityUserDetailService;
    private final MemberService memberService;

    public TeamScheduleService(TeamScheduleRepository teamScheduleRepository,
                               @Lazy ScheduleService scheduleService,
                               TeamService teamService,
                               SecurityUserDetailService securityUserDetailService,
                               MemberService memberService) {
        this.teamScheduleRepository = teamScheduleRepository;
        this.scheduleService = scheduleService;
        this.teamService = teamService;
        this.securityUserDetailService = securityUserDetailService;
        this.memberService = memberService;
    }

    // 팀 일정 조회
    @Transactional(readOnly = true)
    public List<TeamScheduleResponseDto> getTeamSchedulesForLoggedInUser() {
        Long loggedInMemberId = securityUserDetailService.getLoggedInMember().getId();

        var member = memberService.findMemberById(loggedInMemberId);

        Hibernate.initialize(member.getTeamList());

        List<Long> teamIds = member.getTeamList().stream()
                .map(team -> team.getTeam().getId())
                .collect(Collectors.toList());

        List<TeamSchedule> teamSchedules = teamScheduleRepository.findByTeamIdIn(teamIds);
        return teamSchedules.stream().map(TeamScheduleResponseDto::new).collect(Collectors.toList());
    }

    // 특정 팀 일정 조회
    @Transactional(readOnly = true)
    public List<TeamScheduleResponseDto> getTeamSchedulesByTeamId(Long teamId) {
        Long loggedInMemberId = securityUserDetailService.getLoggedInMember().getId();

        var member = memberService.findMemberById(loggedInMemberId);

        Hibernate.initialize(member.getTeamList());

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
        var schedule = scheduleService.getScheduleEntityById(teamScheduleRequestDto.getScheduleId());

        var team = teamService.findById(teamScheduleRequestDto.getTeamId());

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
        var existingTeamSchedule = teamScheduleRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));

        var schedule = scheduleService.getScheduleEntityById(teamScheduleRequestDto.getScheduleId());

        TeamSchedule updatedTeamSchedule = existingTeamSchedule.toBuilder()
                .schedule(schedule)
                .build();

        teamScheduleRepository.save(updatedTeamSchedule);
        return new TeamScheduleResponseDto(updatedTeamSchedule);
    }

    // 팀 일정 삭제
    @Transactional
    public void deleteTeamSchedule(Long id) {
        var existingTeamSchedule = teamScheduleRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));

        teamScheduleRepository.deleteById(id);
    }
}