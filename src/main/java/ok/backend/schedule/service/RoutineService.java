package ok.backend.schedule.service;

import ok.backend.common.exception.CustomException;
import ok.backend.common.exception.ErrorCode;
import ok.backend.common.security.util.SecurityUserDetailService;
import ok.backend.member.domain.entity.Member;
import ok.backend.schedule.domain.entity.Routine;
import ok.backend.schedule.domain.entity.Schedule;
import ok.backend.schedule.domain.enums.DayOfWeek;
import ok.backend.schedule.domain.enums.RepeatType;
import ok.backend.schedule.domain.repository.RoutineRepository;
import ok.backend.schedule.domain.repository.ScheduleRepository;
import ok.backend.schedule.dto.req.RoutineRequestDto;
import ok.backend.schedule.dto.res.RoutineResponseDto;
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
    private final SecurityUserDetailService securityUserDetailService;

    public RoutineService(RoutineRepository routineRepository,
                          ScheduleRepository scheduleRepository,
                          SecurityUserDetailService securityUserDetailService) {
        this.routineRepository = routineRepository;
        this.scheduleRepository = scheduleRepository;
        this.securityUserDetailService = securityUserDetailService;
    }

    // 반복 일정 조회
    public List<RoutineResponseDto> getRoutinesForLoggedInUser() {
        Member loggedInMember = securityUserDetailService.getLoggedInMember();
        Long userId = loggedInMember.getId();

        List<Routine> routines = routineRepository.findBySchedule_MemberId(userId);
        return routines.stream().map(RoutineResponseDto::new).collect(Collectors.toList());
    }

    // 반복 일정 생성
    @Transactional
    public RoutineResponseDto createRoutine(RoutineRequestDto routineRequestDto) {
        Member loggedInMember = securityUserDetailService.getLoggedInMember();

        if (routineRequestDto.getRepeatType() == null) {
            throw new CustomException(ErrorCode.EMPTY_INPUT_SCHEDULE);
        }

        RepeatType repeatType = RepeatType.valueOf(routineRequestDto.getRepeatType().toUpperCase());

        Set<DayOfWeek> repeatOn = null;
        if (repeatType == RepeatType.WEEKLY) {
            repeatOn = Stream.of(routineRequestDto.getRepeatOn())
                    .map(day -> DayOfWeek.valueOf(day.toUpperCase()))
                    .collect(Collectors.toSet());

            if (repeatOn.isEmpty()) {
                throw new CustomException(ErrorCode.INVALID_SCHEDULE_REQUEST);
            }
        }

        Schedule schedule = scheduleRepository.findById(routineRequestDto.getScheduleId())
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (!schedule.getMember().getId().equals(loggedInMember.getId())) {
            throw new CustomException(ErrorCode.NOT_ACCESS_SCHEDULE);
        }

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
        Member loggedInMember = securityUserDetailService.getLoggedInMember();
        Long userId = loggedInMember.getId();

        Routine existingRoutine = routineRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (!existingRoutine.getSchedule().getMember().getId().equals(userId)) {
            throw new CustomException(ErrorCode.NOT_ACCESS_SCHEDULE);
        }

        RepeatType repeatType = RepeatType.valueOf(routineRequestDto.getRepeatType().toUpperCase());

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
                .repeatOn(repeatType == RepeatType.WEEKLY ? repeatOn : null)
                .build();

        routineRepository.save(updatedRoutine);
        return new RoutineResponseDto(updatedRoutine);
    }

    // 반복 일정 삭제
    @Transactional
    public void deleteRoutine(Long id) {
        Member loggedInMember = securityUserDetailService.getLoggedInMember();
        Long userId = loggedInMember.getId();

        Routine existingRoutine = routineRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (!existingRoutine.getSchedule().getMember().getId().equals(userId)) {
            throw new CustomException(ErrorCode.NOT_ACCESS_SCHEDULE);
        }

        routineRepository.deleteById(id);
    }
}