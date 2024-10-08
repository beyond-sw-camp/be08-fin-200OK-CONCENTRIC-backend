package ok.backend.schedule.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import ok.backend.schedule.dto.req.SubScheduleRequestDto;
import ok.backend.schedule.dto.res.SubScheduleResponseDto;
import ok.backend.schedule.service.SubScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "SubSchedule", description = "하위 일정 관리")
@Slf4j
@RestController
@RequestMapping("/v1/api/subSchedule")
public class SubScheduleController {

    private final SubScheduleService subScheduleService;

    @Autowired
    public SubScheduleController(SubScheduleService subScheduleService) {
        this.subScheduleService = subScheduleService;
    }

    @Operation(summary = "상위 일정에 속한 하위 일정 전체 조회 API")
    @GetMapping("/list/{scheduleId}")
    public List<SubScheduleResponseDto> getSubSchedulesByScheduleId(@PathVariable Long scheduleId) {
        return subScheduleService.getSubSchedulesByScheduleId(scheduleId);
    }

    @Operation(summary = "특정 하위 일정 조회 API")
    @GetMapping("/list/{id}")
    public SubScheduleResponseDto getSubScheduleById(@PathVariable Long id) {
        return subScheduleService.getSubScheduleById(id);
    }

    @Operation(summary = "하위 일정 생성 API")
    @PostMapping("/create")
    public SubScheduleResponseDto createSubSchedule(@RequestBody SubScheduleRequestDto subScheduleRequestDto) {
        return subScheduleService.createSubSchedule(subScheduleRequestDto);
    }

    @Operation(summary = "하위 일정 수정 API")
    @PutMapping("/update/{id}")
    public SubScheduleResponseDto updateSubSchedule(@PathVariable Long id, @RequestBody SubScheduleRequestDto subScheduleRequestDto) {
        return subScheduleService.updateSubSchedule(id, subScheduleRequestDto);
    }

    @Operation(summary = "하위 일정 삭제 API")
    @DeleteMapping("/delete/{id}")
    public void deleteSubSchedule(@PathVariable Long id) { subScheduleService.deleteSubSchedule(id); }
}
