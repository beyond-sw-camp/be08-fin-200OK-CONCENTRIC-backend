package ok.backend.schedule.service;

import ok.backend.common.security.util.SecurityUser;
import ok.backend.schedule.domain.entity.Routine;
import ok.backend.schedule.domain.entity.Schedule;
import ok.backend.schedule.domain.enums.DayOfWeek;
import ok.backend.schedule.domain.enums.RepeatType;
import ok.backend.schedule.domain.repository.RoutineRepository;
import ok.backend.schedule.domain.repository.ScheduleRepository;
import ok.backend.schedule.dto.req.RoutineRequestDto;
import ok.backend.schedule.dto.res.RoutineResponseDto;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class RoutineService {

    private final RoutineRepository routineRepository;
    private final ScheduleRepository scheduleRepository;  // ScheduleRepository 주입

    public RoutineService(RoutineRepository routineRepository, ScheduleRepository scheduleRepository) {
        this.routineRepository = routineRepository;
        this.scheduleRepository = scheduleRepository;
    }

    // 로그인한 유저의 반복 일정 조회
    public List<RoutineResponseDto> getRoutinesForLoggedInUser() {
        Long userId = getLoggedInUserId();
        List<Routine> routines = routineRepository.findBySchedule_MemberId(userId);
        return routines.stream().map(RoutineResponseDto::new).collect(Collectors.toList());
    }

    // 반복 일정 생성
    @Transactional
    public RoutineResponseDto createRoutine(RoutineRequestDto routineRequestDto) {
        SecurityUser securityUser = getLoggedInUser();

        if (routineRequestDto.getRepeatType() == null) {
            throw new IllegalArgumentException("Repeat type cannot be null.");
        }

        RepeatType repeatType = RepeatType.valueOf(routineRequestDto.getRepeatType().toUpperCase());
        Set<DayOfWeek> repeatOn = Stream.of(routineRequestDto.getRepeatOn())
                .map(day -> DayOfWeek.valueOf(day.toUpperCase()))
                .collect(Collectors.toSet());

        // 관련 스케줄을 조회
        Schedule schedule = scheduleRepository.findById(routineRequestDto.getScheduleId())
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        if (!schedule.getMember().getId().equals(getLoggedInUserId())) {
            throw new RuntimeException("You can only create routines for your own schedules.");
        }

        // 빌더 패턴을 사용하여 Routine 객체 생성
        Routine routine = Routine.builder()
                .schedule(schedule)  // 스케줄 설정
                .repeatType(repeatType)  // 반복 유형 설정
                .repeatInterval(routineRequestDto.getRepeatInterval())
                .repeatOn(repeatOn)  // 요일 설정
                .build();

        routineRepository.save(routine);
        return new RoutineResponseDto(routine);
    }

    // 반복 일정 수정
    @Transactional
    public RoutineResponseDto updateRoutine(Long id, RoutineRequestDto routineRequestDto) {
        Long userId = getLoggedInUserId();
        Routine existingRoutine = routineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Routine not found"));

        if (!existingRoutine.getSchedule().getMember().getId().equals(userId)) {
            throw new RuntimeException("You can only modify your own routines.");
        }

        RepeatType repeatType = RepeatType.valueOf(routineRequestDto.getRepeatType().toUpperCase());
        Set<DayOfWeek> repeatOn = Stream.of(routineRequestDto.getRepeatOn())
                .map(day -> DayOfWeek.valueOf(day.toUpperCase()))
                .collect(Collectors.toSet());

        // 빌더 패턴을 사용하여 수정된 Routine 객체 생성
        Routine updatedRoutine = Routine.builder()
                .id(existingRoutine.getId())  // 기존 ID 유지
                .schedule(existingRoutine.getSchedule())  // 스케줄 유지
                .repeatType(repeatType)
                .repeatInterval(routineRequestDto.getRepeatInterval())
                .repeatOn(repeatOn)
                .build();

        routineRepository.save(updatedRoutine);
        return new RoutineResponseDto(updatedRoutine);
    }

    // 반복 일정 삭제
    @Transactional
    public void deleteRoutine(Long id) {
        Long userId = getLoggedInUserId();
        Routine existingRoutine = routineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Routine not found"));

        if (!existingRoutine.getSchedule().getMember().getId().equals(userId)) {
            throw new RuntimeException("You can only delete your own routines.");
        }

        routineRepository.deleteById(id);
    }

    // 공통 로그인 유저 확인 메서드
    private Long getLoggedInUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        return securityUser.getMember().getId();
    }

    private SecurityUser getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (SecurityUser) authentication.getPrincipal();
    }
}