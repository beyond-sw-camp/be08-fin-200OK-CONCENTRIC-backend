package ok.backend.schedule.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import ok.backend.schedule.dto.req.RoutineRequestDto;
import ok.backend.schedule.dto.res.RoutineResponseDto;
import ok.backend.schedule.service.RoutineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Routine", description = "반복 일정 관리")
@Slf4j
@RestController
@RequestMapping("/v1/api/routine")
public class RoutineController {

    @Autowired
    private RoutineService routineService;

    @Operation(summary = "반복 일정 조회 API")
    @GetMapping("/list")
    public List<RoutineResponseDto> getRoutinesForLoggedInUser() {
        return routineService.getRoutinesForLoggedInUser();
    }

    @Operation(summary = "반복 일정 생성 API")
    @PostMapping("/create")
    public RoutineResponseDto createRoutine(@RequestBody RoutineRequestDto routineRequestDto) {
        return routineService.createRoutine(routineRequestDto);
    }

    @Operation(summary = "반복 일정 수정 API")
    @PutMapping("/update/{id}")
    public RoutineResponseDto updateRoutine(@PathVariable Long id, @RequestBody RoutineRequestDto routineRequestDto) {
        return routineService.updateRoutine(id, routineRequestDto);
    }

    @Operation(summary = "반복 일정 삭제 API")
    @DeleteMapping("/delete/{id}")
    public void deleteRoutine(@PathVariable Long id) {
        routineService.deleteRoutine(id);
    }
}



