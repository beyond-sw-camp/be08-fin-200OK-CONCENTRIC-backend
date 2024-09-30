package ok.backend.team.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import ok.backend.team.dto.TeamRequestDto;
import ok.backend.team.dto.TeamResponseDto;
import ok.backend.team.dto.TeamUpdateRequestDto;
import ok.backend.team.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


//@RestController
//@RequestMapping("v1/api/team")
//@Tag(name = "Team", description = "그룹 관리")
//public class TeamController {
//
//    @Autowired
//    private TeamService teamService;
//
//    @GetMapping("/list")
//    @Operation(summary = "그룹 목록 조회 API")
//    public ResponseEntity<List<TeamResponseDto>> getAllTeams() {
//        List<TeamResponseDto> teams = teamService.getAllTeams();
//        return ResponseEntity.ok(teams);
//    }
//
//    // 특정 그룹 조회
//    @GetMapping("/{id}")
//    @Operation(summary = "특정 그룹 조회 API")
//    public TeamResponseDto getTeam(@PathVariable Long id) {
//
//        return teamService.getTeamById(id);
//    }
//
//
//    // 그룹 생성
//    @PostMapping("/register")
//    @Operation(summary = "그룹 생성 API")
//    public ResponseEntity<TeamResponseDto> createTeam(@RequestBody TeamRequestDto teamRequestDto) {
//        TeamResponseDto createdTeam = teamService.createTeam(teamRequestDto);
//        return ResponseEntity.ok(createdTeam);
//    }
//
//    // 그룹 수정
//    @PutMapping("/{id}")
//    @Operation(summary = "특정 그룹 수정 API")
//    public ResponseEntity<Void> updateTeam(@PathVariable Long id, @RequestBody TeamUpdateRequestDto teamUpdateRequestDto) {
//        teamService.updateTeam(id, teamUpdateRequestDto);
//        return ResponseEntity.noContent().build();
//    }
//
//
//    // 그룹 삭제
//    @DeleteMapping("/{id}")
//    @Operation(summary = "특정 그룹 삭제 API")
//    public ResponseEntity<Void> deleteTeam(@PathVariable Long id) {
//        teamService.deleteTeam(id);
//        return ResponseEntity.noContent().build();
//    }
//}
