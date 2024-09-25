package ok.backend.schedule.controller;//package ok.backend.schedule.controller;
//
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.extern.slf4j.Slf4j;
//import ok.backend.schedule.dto.req.TeamScheduleRequestDto;
//import ok.backend.schedule.dto.res.TeamScheduleResponseDto;
//import ok.backend.schedule.service.TeamScheduleService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@Tag(name = "Team-schedule", description = "팀 일정 관리")
//@Slf4j
//@Controller
//@RequestMapping("/team-schedules")
//public class TeamScheduleController {
//
//    @Autowired
//    private TeamScheduleService teamScheduleService;
//
//    @Operation(summary = "팀 일정 조회 API")
//    @GetMapping
//    public List<TeamScheduleResponseDto> getTeamSchedulesForLoggedInUser() {
//        return teamScheduleService.getTeamSchedulesForLoggedInUser();
//    }
//
//    @Operation(summary = "팀 일정 생성 API")
//    @PostMapping
//    public TeamScheduleResponseDto createTeamSchedule(@RequestBody TeamScheduleRequestDto teamScheduleRequestDto) {
//        return teamScheduleService.createTeamSchedule(teamScheduleRequestDto);
//    }
//
//    @Operation(summary = "팀 일정 수정 API")
//    @PutMapping("/{id}")
//    public TeamScheduleResponseDto updateTeamSchedule(@PathVariable Long id, @RequestBody TeamScheduleRequestDto teamScheduleRequestDto) {
//        return teamScheduleService.updateTeamSchedule(id, teamScheduleRequestDto);
//    }
//
//    @Operation(summary = "팀 일정 삭제 API")
//    @DeleteMapping("/{id}")
//    public void deleteTeamSchedule(@PathVariable Long id) {
//        teamScheduleService.deleteTeamSchedule(id);
//    }
//}
//
