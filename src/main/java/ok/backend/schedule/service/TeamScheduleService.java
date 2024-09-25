package ok.backend.schedule.service;//package ok.backend.schedule.service;
//
//import ok.backend.schedule.domain.entity.TeamSchedule;
//import ok.backend.schedule.domain.repository.TeamScheduleRepository;
//import ok.backend.schedule.dto.req.TeamScheduleRequestDto;
//import ok.backend.schedule.dto.res.TeamScheduleResponseDto;
//import ok.backend.common.security.util.SecurityUser;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//public class TeamScheduleService {
//
//    private final TeamScheduleRepository teamScheduleRepository;
//
//    public TeamScheduleService(TeamScheduleRepository teamScheduleRepository) {
//        this.teamScheduleRepository = teamScheduleRepository;
//    }
//
//    // 로그인한 유저의 팀 일정 조회
//    public List<TeamScheduleResponseDto> getTeamSchedulesForLoggedInUser() {
//        Long teamId = getLoggedInUserTeamId();
//
//        List<TeamSchedule> teamSchedules = teamScheduleRepository.findByTeamId(teamId);
//        return teamSchedules.stream().map(TeamScheduleResponseDto::new).collect(Collectors.toList());
//    }
//
//    // 팀 일정 생성
//    public TeamScheduleResponseDto createTeamSchedule(TeamScheduleRequestDto teamScheduleRequestDto) {
//        Long teamId = getLoggedInUserTeamId();
//
//        TeamSchedule teamSchedule = TeamSchedule.builder()
//                .teamId(teamId)
//                .scheduleId(teamScheduleRequestDto.getScheduleId())
//                .build();
//
//        teamScheduleRepository.save(teamSchedule);
//        return new TeamScheduleResponseDto(teamSchedule);
//    }
//
//    // 팀 일정 수정
//    public TeamScheduleResponseDto updateTeamSchedule(Long id, TeamScheduleRequestDto teamScheduleRequestDto) {
//        Long teamId = getLoggedInUserTeamId();
//        TeamSchedule existingTeamSchedule = teamScheduleRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Team Schedule not found"));
//
//        // 로그인한 유저의 팀 일정인지 확인
//        if (!existingTeamSchedule.getTeamId().equals(teamId)) {
//            throw new RuntimeException("You can only modify schedules for your own team.");
//        }
//
//        // 기존 객체를 기반으로 새로운 TeamSchedule 객체 생성
//        TeamSchedule updatedTeamSchedule = TeamSchedule.builder()
//                .id(existingTeamSchedule.getId())  // 기존 ID 유지
//                .teamId(teamId)
//                .scheduleId(teamScheduleRequestDto.getScheduleId())
//                .build();
//
//        teamScheduleRepository.save(updatedTeamSchedule);
//        return new TeamScheduleResponseDto(updatedTeamSchedule);
//    }
//
//    // 팀 일정 삭제
//    public void deleteTeamSchedule(Long id) {
//        Long teamId = getLoggedInUserTeamId();
//        TeamSchedule existingTeamSchedule = teamScheduleRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Team Schedule not found"));
//
//        // 로그인한 유저의 팀 일정인지 확인
//        if (!existingTeamSchedule.getTeamId().equals(teamId)) {
//            throw new RuntimeException("You can only delete schedules for your own team.");
//        }
//
//        teamScheduleRepository.deleteById(id);
//    }
//
//    // 공통 로그인 유저 팀 ID 확인 메서드
//    private Long getLoggedInUserTeamId() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
//        return securityUser.getMember().getTeam().getId();
//    }
//
//    // 공통 로그인 유저 가져오기 메서드
//    private SecurityUser getLoggedInUser() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        return (SecurityUser) authentication.getPrincipal();
//    }
//}