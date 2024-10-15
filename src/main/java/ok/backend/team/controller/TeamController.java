package ok.backend.team.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import ok.backend.member.dto.MemberResponseDto;
import ok.backend.member.dto.MemberUpdateRequestDto;
import ok.backend.team.dto.TeamRequestDto;
import ok.backend.team.dto.TeamResponseDto;
import ok.backend.team.dto.TeamUpdateRequestDto;
import ok.backend.team.service.TeamSendingService;
import ok.backend.team.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@RestController
@RequestMapping("v1/api/team")
@RequiredArgsConstructor
@Tag(name = "Team", description = "그룹 관리")
public class TeamController {

    @Autowired
    private TeamService teamService;
    @Autowired
    private TeamSendingService teamSendingService;

    @GetMapping("/list")
    @Operation(summary = "그룹 목록 조회 API")
    public ResponseEntity<List<TeamResponseDto>> getAllTeams() {
        List<TeamResponseDto> teams = teamService.getAllTeams();
        return ResponseEntity.ok(teams);
    }

    // 특정 그룹 조회
    @GetMapping("/{id}")
    @Operation(summary = "특정 그룹 조회 API")
    public TeamResponseDto getTeam(@PathVariable Long id) {

        return teamService.getTeamById(id);
    }


    // 그룹 생성
    @PostMapping("/register")
    @Operation(summary = "그룹 생성 API")
    public ResponseEntity<TeamResponseDto> createTeam(@RequestBody TeamRequestDto teamRequestDto) {
        TeamResponseDto createdTeam = teamService.createTeam(teamRequestDto);
        return ResponseEntity.ok(createdTeam);
    }

//    // 그룹 수정
//    @PutMapping("/{id}")
//    @Operation(summary = "특정 그룹 수정 API")
//    public ResponseEntity<Void> updateTeam(@PathVariable Long id, @RequestBody TeamUpdateRequestDto teamUpdateRequestDto) {
//        teamService.updateTeam(id, teamUpdateRequestDto);
//        return ResponseEntity.noContent().build();
//    }


    // 그룹 삭제
    @DeleteMapping("/{id}")
    @Operation(summary = "특정 그룹 삭제 API")
    public ResponseEntity<Void> deleteTeam(@PathVariable Long id) {
        teamService.deleteTeam(id);
        return ResponseEntity.noContent().build();
    }

    // 팀 초대 URL 생성 및 이메일 전송
    @PostMapping("/{id}/invite")
    @Operation(summary = "팀 초대 이메일 전송 API")
    public ResponseEntity<String> inviteMember(@PathVariable Long id, @RequestParam String inviteeEmail) throws MessagingException {
        teamSendingService.sendInviteEmail(id, inviteeEmail);
        return ResponseEntity.ok("초대 이메일이 성공적으로 전송되었습니다.");
    }

    // 팀 참여
    @GetMapping("/invite")
    @Operation(summary = "팀 초대 수락 API")
    public ResponseEntity<String> joinTeam(@RequestParam Long teamId) {
        teamService.joinTeam(teamId);
        return ResponseEntity.ok("팀에 성공적으로 참여하였습니다.");
    }

    // 그룹 나가기
    @DeleteMapping("/leave/{id}")
    @Operation(summary = "그룹 나가기 API")
    public ResponseEntity<Void> leaveTeam(@PathVariable Long id) {
        teamService.leaveTeam(id);
        return ResponseEntity.noContent().build();
    }

    // 그룹 프로필 수정
    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "팀 정보 수정 API")
    public ResponseEntity<TeamResponseDto> updateTeam(@RequestPart("team") TeamUpdateRequestDto teamUpdateRequestDto,
                                                      @RequestPart("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(teamService.updateTeam(TeamUpdateRequestDto, file));
    }








}
