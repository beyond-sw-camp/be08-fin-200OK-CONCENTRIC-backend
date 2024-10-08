package ok.backend.schedule.service;

import ok.backend.common.exception.CustomException;
import ok.backend.common.exception.ErrorCode;
import ok.backend.common.security.util.SecurityUserDetailService;
import ok.backend.schedule.domain.entity.Routine;
import ok.backend.schedule.domain.enums.DayOfWeek;
import ok.backend.schedule.domain.enums.RepeatType;
import ok.backend.schedule.domain.repository.RoutineRepository;
import ok.backend.schedule.dto.req.RoutineRequestDto;
import ok.backend.schedule.dto.res.RoutineResponseDto;
import ok.backend.member.service.MemberService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class RoutineService {

    private final RoutineRepository routineRepository;
    private final ScheduleService scheduleService;
    private final MemberService memberService;
    private final SecurityUserDetailService securityUserDetailService;

    public RoutineService(RoutineRepository routineRepository,
                          @Lazy ScheduleService scheduleService,
                          MemberService memberService,
                          SecurityUserDetailService securityUserDetailService) {
        this.routineRepository = routineRepository;
        this.scheduleService = scheduleService;
        this.memberService = memberService;
        this.securityUserDetailService = securityUserDetailService;
    }

    public boolean existsByScheduleId(Long scheduleId) {
        return routineRepository.existsBySchedule_Id(scheduleId);
    }

    // 반복 일정 조회
    public List<RoutineResponseDto> getRoutinesForLoggedInUser() {
        Long loggedInUserId = securityUserDetailService.getLoggedInMember().getId();
        memberService.findMemberById(loggedInUserId); // 존재 및 활성화된 사용자 확인

        List<Routine> routines = routineRepository.findBySchedule_MemberId(loggedInUserId);
        return routines.stream().map(RoutineResponseDto::new).collect(Collectors.toList());
    }

    // 반복 일정 생성
    @Transactional
    public RoutineResponseDto createRoutine(RoutineRequestDto routineRequestDto) {
        Long loggedInUserId = securityUserDetailService.getLoggedInMember().getId();
        memberService.findMemberById(loggedInUserId); // 존재 및 활성화된 사용자 확인

        if (routineRequestDto.getRepeatType() == null) {
            throw new CustomException(ErrorCode.EMPTY_INPUT_SCHEDULE);
        }

        RepeatType repeatType = routineRequestDto.getRepeatType();
        Long scheduleId = routineRequestDto.getScheduleId();
        var schedule = scheduleService.getScheduleEntityById(scheduleId);

        // 일정의 시작일과 종료일이 같은지 확인
        if (!schedule.getStartDate().toLocalDate().equals(schedule.getEndDate().toLocalDate())) {
            throw new CustomException(ErrorCode.INVALID_SCHEDULE_REQUEST);
        }

        Set<DayOfWeek> dayOfWeek = null;
        Set<Integer> dayOfMonth = null;

        // 주간 반복일 때 요일 선택
        if (repeatType == RepeatType.WEEKLY) {
            dayOfWeek = Stream.of(routineRequestDto.getDayOfWeek())
                    .map(day -> DayOfWeek.valueOf(day.name().toUpperCase()))
                    .collect(Collectors.toSet());

            if (dayOfWeek.isEmpty()) {
                throw new CustomException(ErrorCode.INVALID_SCHEDULE_REQUEST);
            }
        }

        // 월간 반복일 때 일자 선택
        if (repeatType == RepeatType.MONTHLY) {
            dayOfMonth = Set.of(routineRequestDto.getDayOfMonth());
            if (dayOfMonth.isEmpty()) {
                throw new CustomException(ErrorCode.INVALID_SCHEDULE_REQUEST);
            }
        }

        // 연간 반복일 경우 일정의 월, 일, 시간을 가져와 사용
        LocalDateTime endDate = (repeatType == RepeatType.YEARLY)
                ? schedule.getEndDate().withYear(LocalDateTime.now().getYear())
                : (routineRequestDto.getEndDate() != null ? LocalDateTime.parse(routineRequestDto.getEndDate()) : null);

        Routine routine = Routine.builder()
                .schedule(schedule)
                .repeatType(repeatType)
                .repeatInterval(routineRequestDto.getRepeatInterval())
                .dayOfWeek(repeatType == RepeatType.WEEKLY ? dayOfWeek : null)
                .dayOfMonth(repeatType == RepeatType.MONTHLY ? dayOfMonth : null)
                .endDate(endDate)
                .build();

        routineRepository.save(routine);
        return new RoutineResponseDto(routine);
    }

    // 반복 일정 수정
    @Transactional
    public RoutineResponseDto updateRoutine(Long id, RoutineRequestDto routineRequestDto) {
        Long loggedInUserId = securityUserDetailService.getLoggedInMember().getId();
        memberService.findMemberById(loggedInUserId);

        Routine existingRoutine = routineRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (!existingRoutine.getSchedule().getMember().getId().equals(loggedInUserId)) {
            throw new CustomException(ErrorCode.NOT_ACCESS_SCHEDULE);
        }

        RepeatType repeatType = routineRequestDto.getRepeatType();
        Set<DayOfWeek> dayOfWeek = null;
        Set<Integer> dayOfMonth = null;

        if (repeatType == RepeatType.WEEKLY) {
            dayOfWeek = Stream.of(routineRequestDto.getDayOfWeek())
                    .map(day -> DayOfWeek.valueOf(day.name().toUpperCase()))
                    .collect(Collectors.toSet());

            if (dayOfWeek.isEmpty()) {
                throw new CustomException(ErrorCode.INVALID_SCHEDULE_REQUEST);
            }
        }

        if (repeatType == RepeatType.MONTHLY) {
            dayOfMonth = Set.of(routineRequestDto.getDayOfMonth());
            if (dayOfMonth.isEmpty()) {
                throw new CustomException(ErrorCode.INVALID_SCHEDULE_REQUEST);
            }
        }

        LocalDateTime endDate = (repeatType == RepeatType.YEARLY)
                ? existingRoutine.getSchedule().getEndDate().withYear(LocalDateTime.now().getYear())
                : (routineRequestDto.getEndDate() != null ? LocalDateTime.parse(routineRequestDto.getEndDate()) : null);

        Routine updatedRoutine = existingRoutine.toBuilder()
                .repeatType(repeatType)
                .repeatInterval(routineRequestDto.getRepeatInterval())
                .dayOfWeek(repeatType == RepeatType.WEEKLY ? dayOfWeek : null)
                .dayOfMonth(repeatType == RepeatType.MONTHLY ? dayOfMonth : null)
                .endDate(endDate)
                .build();

        routineRepository.save(updatedRoutine);
        return new RoutineResponseDto(updatedRoutine);
    }

    // 반복 일정 삭제
    @Transactional
    public void deleteRoutine(Long id) {
        Long loggedInUserId = securityUserDetailService.getLoggedInMember().getId();
        memberService.findMemberById(loggedInUserId); // 존재 및 활성화된 사용자 확인

        Routine existingRoutine = routineRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (!existingRoutine.getSchedule().getMember().getId().equals(loggedInUserId)) {
            throw new CustomException(ErrorCode.NOT_ACCESS_SCHEDULE);
        }

        routineRepository.deleteById(id);
    }
}