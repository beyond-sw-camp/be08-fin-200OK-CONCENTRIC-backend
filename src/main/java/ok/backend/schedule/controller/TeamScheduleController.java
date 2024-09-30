package ok.backend.schedule.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import ok.backend.schedule.dto.req.TeamScheduleRequestDto;
import ok.backend.schedule.dto.res.TeamScheduleResponseDto;
import ok.backend.schedule.service.TeamScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "TeamSchedule", description = "팀 일정 관리")
@Slf4j
@RestController
@RequestMapping("/v1/api/teamSchedule")
public class TeamScheduleController {

    private final TeamScheduleService teamScheduleService;

    @Autowired
    public TeamScheduleController(TeamScheduleService teamScheduleService) {
        this.teamScheduleService = teamScheduleService;
    }

    @Operation(summary = "로그인한 유저의 팀 일정 조회 API")
    @GetMapping("/list")
    public ResponseEntity<List<TeamScheduleResponseDto>> getTeamSchedulesForLoggedInUser() {
        List<TeamScheduleResponseDto> teamSchedules = teamScheduleService.getTeamSchedulesForLoggedInUser();
        return ResponseEntity.ok(teamSchedules);
    }

    @Operation(summary = "특정 팀의 일정 조회 API")
    @GetMapping("/list/{teamId}")
    public ResponseEntity<List<TeamScheduleResponseDto>> getTeamSchedulesByTeamId(@PathVariable Long teamId) {
        List<TeamScheduleResponseDto> teamSchedules = teamScheduleService.getTeamSchedulesByTeamId(teamId);
        return ResponseEntity.ok(teamSchedules);
    }

    @Operation(summary = "팀 일정 생성 API")
    @PostMapping("/create")
    public ResponseEntity<TeamScheduleResponseDto> createTeamSchedule(@RequestBody TeamScheduleRequestDto teamScheduleRequestDto) {
        TeamScheduleResponseDto teamSchedule = teamScheduleService.createTeamSchedule(teamScheduleRequestDto);
        return ResponseEntity.ok(teamSchedule);
    }

    @Operation(summary = "팀 일정 수정 API")
    @PutMapping("/update/{id}")
    public ResponseEntity<TeamScheduleResponseDto> updateTeamSchedule(@PathVariable Long id, @RequestBody TeamScheduleRequestDto teamScheduleRequestDto) {
        TeamScheduleResponseDto updatedTeamSchedule = teamScheduleService.updateTeamSchedule(id, teamScheduleRequestDto);
        return ResponseEntity.ok(updatedTeamSchedule);
    }

    @Operation(summary = "팀 일정 삭제 API")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteTeamSchedule(@PathVariable Long id) {
        teamScheduleService.deleteTeamSchedule(id);
        return ResponseEntity.noContent().build();
    }
}