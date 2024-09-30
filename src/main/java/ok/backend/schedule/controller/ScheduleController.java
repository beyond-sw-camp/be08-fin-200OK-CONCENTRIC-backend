package ok.backend.schedule.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import ok.backend.schedule.dto.req.ScheduleRequestDto;
import ok.backend.schedule.dto.res.ScheduleResponseDto;
import ok.backend.schedule.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Schedule", description = "일정 관리")
@Slf4j
@RestController
@RequestMapping("/v1/api/schedule")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @Operation(summary = "일정 조회 API")
    @GetMapping("/list")
    public List<ScheduleResponseDto> getSchedulesForLoggedInUser() {
        return scheduleService.getSchedulesForLoggedInUser();
    }

    @Operation(summary = "일정 생성 API")
    @PostMapping("/create")
    public ScheduleResponseDto createSchedule(@RequestBody ScheduleRequestDto scheduleRequestDto) {
        return scheduleService.createSchedule(scheduleRequestDto);
    }

    @Operation(summary = "일정 수정 API")
    @PutMapping("/update/{id}")
    public ScheduleResponseDto updateSchedule(@PathVariable Long id, @RequestBody ScheduleRequestDto scheduleRequestDto) {
        return scheduleService.updateSchedule(id, scheduleRequestDto);
    }

    @Operation(summary = "일정 삭제 API")
    @DeleteMapping("/delete/{id}")
    public void deleteSchedule(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
    }
}

