package ok.backend.schedule.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ok.backend.schedule.domain.enums.Status;
import ok.backend.schedule.dto.req.ScheduleRequestDto;
import ok.backend.schedule.dto.res.ScheduleListResponseDto;
import ok.backend.schedule.dto.res.ScheduleResponseDto;
import ok.backend.schedule.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Schedule", description = "일정 관리")
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/api/schedule")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping("/create")
    @Operation(summary = "일정 생성")
    public ResponseEntity<ScheduleResponseDto> createSchedule(@RequestBody @Valid ScheduleRequestDto scheduleRequestDto) {
        return ResponseEntity.ok(scheduleService.createSchedule(scheduleRequestDto));
    }

    @PutMapping("/update")
    @Operation(summary = "일정 수정")
    public ResponseEntity<ScheduleResponseDto> updateSchedule(@RequestParam Long scheduleId, @RequestBody @Valid ScheduleRequestDto scheduleRequestDto) {
        return ResponseEntity.ok(scheduleService.updateSchedule(scheduleId, scheduleRequestDto));
    }

    @PutMapping("/update/status")
    @Operation(summary = "일정 상태 업데이트")
    public ResponseEntity<Void> updateSchedule(@RequestParam Long scheduleId, @RequestParam Status status) {
        scheduleService.updateScheduleStatus(scheduleId, status);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/list")
    @Operation(summary = "전체 일정 조회")
    public ResponseEntity<List<ScheduleResponseDto>> findAllSchedules() {
        return ResponseEntity.ok(scheduleService.findAllSchedules());
    }

    @GetMapping("/list/team")
    @Operation(summary = "특정 팀 일정 조회")
    public ResponseEntity<List<ScheduleResponseDto>> findAllTeamSchedules(@RequestParam Long teamId) {
        return ResponseEntity.ok(scheduleService.findAllTeamSchedules(teamId));
    }

    @GetMapping("/list/{scheduleId}")
    @Operation(summary = "일정 상세 조회")
    public ResponseEntity<ScheduleListResponseDto> findScheduleById(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(scheduleService.findScheduleById(scheduleId));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "일정 삭제")
    public ResponseEntity<Void> deleteSchedule(@RequestParam Long scheduleId) {
        scheduleService.deleteSchdule(scheduleId);
        return ResponseEntity.noContent().build();
    }
}