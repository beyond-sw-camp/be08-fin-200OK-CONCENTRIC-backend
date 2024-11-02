package ok.backend.schedule.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ok.backend.schedule.domain.enums.Status;
import ok.backend.schedule.dto.req.SubScheduleRequestDto;
import ok.backend.schedule.dto.res.SubScheduleResponseDto;
import ok.backend.schedule.service.SubScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "SubSchedule", description = "하위 일정 관리")
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/api/subtask")
public class SubScheduleController {

    private final SubScheduleService subScheduleService;

    @PostMapping("/create")
    @Operation(summary = "하위 일정 생성")
    public ResponseEntity<SubScheduleResponseDto> createSubSchedule(@RequestBody SubScheduleRequestDto subScheduleRequestDto) {
        return ResponseEntity.ok(subScheduleService.createSubSchedule(subScheduleRequestDto));
    }

    @PutMapping("/update")
    @Operation(summary = "하위 일정 수정")
    public ResponseEntity<SubScheduleResponseDto> updateSubSchedule(@RequestParam Long subScheduleId, @RequestBody SubScheduleRequestDto subScheduleRequestDto) {
        return ResponseEntity.ok(subScheduleService.updateSubSchedule(subScheduleId, subScheduleRequestDto));
    }

    @PutMapping("/update/status")
    @Operation(summary = "하위 일정 상태 업데이트")
    public ResponseEntity<Void> updateSubScheduleStatus(@RequestParam Long subScheduleId, @RequestParam Status status) {
        subScheduleService.updateSubScheduleStatus(subScheduleId, status);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/list")
    @Operation(summary = "상위 일정에 따른 하위 일정 조회")
    public ResponseEntity<List<SubScheduleResponseDto>> findAllSubSchedulesByScheduleId(@RequestParam Long scheduleId) {
        return ResponseEntity.ok(subScheduleService.findAllSubSchedulesByScheduleId(scheduleId));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "하위 일정 삭제")
    public ResponseEntity<Void> deleteSubSchedule(@RequestParam Long subScheduleId) {
        subScheduleService.deleteSubSchedule(subScheduleId);
        return ResponseEntity.noContent().build();
    }
}
