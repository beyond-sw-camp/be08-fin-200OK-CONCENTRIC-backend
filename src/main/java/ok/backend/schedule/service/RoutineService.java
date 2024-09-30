package ok.backend.schedule.service;

import ok.backend.common.exception.CustomException;
import ok.backend.common.exception.ErrorCode;
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
    private final ScheduleRepository scheduleRepository;

    public RoutineService(RoutineRepository routineRepository, ScheduleRepository scheduleRepository) {
        this.routineRepository = routineRepository;
        this.scheduleRepository = scheduleRepository;
    }

    // 반복 일정 조회
    public List<RoutineResponseDto> getRoutinesForLoggedInUser() {
        SecurityUser securityUser = getLoggedInUser();
        Long userId = securityUser.getMember().getId();

        List<Routine> routines = routineRepository.findBySchedule_MemberId(userId);
        return routines.stream().map(RoutineResponseDto::new).collect(Collectors.toList());
    }

    // 반복 일정 생성
    @Transactional
    public RoutineResponseDto createRoutine(RoutineRequestDto routineRequestDto) {
        SecurityUser securityUser = getLoggedInUser();

        if (routineRequestDto.getRepeatType() == null) {
            throw new CustomException(ErrorCode.EMPTY_INPUT_SCHEDULE);
        }

        RepeatType repeatType = RepeatType.valueOf(routineRequestDto.getRepeatType().toUpperCase());

        // 요일은 WEEKLY일 때만 필요함
        Set<DayOfWeek> repeatOn = null;
        if (repeatType == RepeatType.WEEKLY) {
            repeatOn = Stream.of(routineRequestDto.getRepeatOn())
                    .map(day -> DayOfWeek.valueOf(day.toUpperCase()))
                    .collect(Collectors.toSet());

            if (repeatOn.isEmpty()) {
                throw new CustomException(ErrorCode.INVALID_SCHEDULE_REQUEST);
            }
        }

        // 관련 스케줄을 조회
        Schedule schedule = scheduleRepository.findById(routineRequestDto.getScheduleId())
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (!schedule.getMember().getId().equals(securityUser.getMember().getId())) {
            throw new CustomException(ErrorCode.NOT_ACCESS_SCHEDULE);
        }

        // 빌더 패턴을 사용하여 Routine 객체 생성
        Routine routine = Routine.builder()
                .schedule(schedule)
                .repeatType(repeatType)
                .repeatInterval(routineRequestDto.getRepeatInterval())
                .repeatOn(repeatType == RepeatType.WEEKLY ? repeatOn : null)  // WEEKLY일 때만 repeatOn 설정
                .build();

        routineRepository.save(routine);
        return new RoutineResponseDto(routine);
    }

    // 반복 일정 수정
    @Transactional
    public RoutineResponseDto updateRoutine(Long id, RoutineRequestDto routineRequestDto) {
        SecurityUser securityUser = getLoggedInUser();
        Long userId = securityUser.getMember().getId();

        Routine existingRoutine = routineRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (!existingRoutine.getSchedule().getMember().getId().equals(userId)) {
            throw new CustomException(ErrorCode.NOT_ACCESS_SCHEDULE);
        }

        RepeatType repeatType = RepeatType.valueOf(routineRequestDto.getRepeatType().toUpperCase());

        // 요일은 WEEKLY일 때만 필요함
        Set<DayOfWeek> repeatOn = null;
        if (repeatType == RepeatType.WEEKLY) {
            repeatOn = Stream.of(routineRequestDto.getRepeatOn())
                    .map(day -> DayOfWeek.valueOf(day.toUpperCase()))
                    .collect(Collectors.toSet());

            if (repeatOn.isEmpty()) {
                throw new CustomException(ErrorCode.INVALID_SCHEDULE_REQUEST);
            }
        }

        Routine updatedRoutine = existingRoutine.toBuilder()
                .repeatType(repeatType)
                .repeatInterval(routineRequestDto.getRepeatInterval())
                .repeatOn(repeatType == RepeatType.WEEKLY ? repeatOn : null)  // WEEKLY일 때만 repeatOn 설정
                .build();

        routineRepository.save(updatedRoutine);
        return new RoutineResponseDto(updatedRoutine);
    }

    // 반복 일정 삭제
    @Transactional
    public void deleteRoutine(Long id) {
        SecurityUser securityUser = getLoggedInUser();
        Long userId = securityUser.getMember().getId();

        Routine existingRoutine = routineRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (!existingRoutine.getSchedule().getMember().getId().equals(userId)) {
            throw new CustomException(ErrorCode.NOT_ACCESS_SCHEDULE);
        }

        routineRepository.deleteById(id);
    }

    // 전체 SecurityUser 객체를 가져오는 메서드
    private SecurityUser getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (SecurityUser) authentication.getPrincipal();
    }
}