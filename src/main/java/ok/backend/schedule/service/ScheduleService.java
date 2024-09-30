package ok.backend.schedule.service;

import ok.backend.common.exception.CustomException;
import ok.backend.common.exception.ErrorCode;
import ok.backend.common.security.util.SecurityUser;
import ok.backend.schedule.domain.entity.Schedule;
import ok.backend.schedule.domain.repository.ScheduleRepository;
import ok.backend.schedule.dto.req.ScheduleRequestDto;
import ok.backend.schedule.dto.res.ScheduleResponseDto;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    public ScheduleService(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // 로그인한 유저의 모든 일정 조회
    public List<ScheduleResponseDto> getSchedulesForLoggedInUser() {
        SecurityUser securityUser = getLoggedInUser();
        Long userId = securityUser.getMember().getId();

        List<Schedule> schedules = scheduleRepository.findByMemberId(userId);
        return schedules.stream().map(ScheduleResponseDto::new).collect(Collectors.toList());
    }

    // 일정 생성
    public ScheduleResponseDto createSchedule(ScheduleRequestDto scheduleRequestDto) {
        SecurityUser securityUser = getLoggedInUser();

        LocalDateTime startDate;
        LocalDateTime endDate;
        try {
            startDate = LocalDateTime.parse(scheduleRequestDto.getStartDate(), FORMATTER);
            endDate = LocalDateTime.parse(scheduleRequestDto.getEndDate(), FORMATTER);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_SCHEDULE_REQUEST);
        }

        Schedule schedule = Schedule.builder()
                .member(securityUser.getMember())
                .title(scheduleRequestDto.getTitle())
                .description(scheduleRequestDto.getDescription())
                .status(scheduleRequestDto.getStatus())
                .startDate(startDate)
                .endDate(endDate)
                .importance(scheduleRequestDto.getImportance())
                .build();

        scheduleRepository.save(schedule);
        return new ScheduleResponseDto(schedule);
    }

    // 일정 수정
    public ScheduleResponseDto updateSchedule(Long id, ScheduleRequestDto scheduleRequestDto) {
        SecurityUser securityUser = getLoggedInUser();
        Long userId = securityUser.getMember().getId();

        Schedule existingSchedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (!existingSchedule.getMember().getId().equals(userId)) {
            throw new CustomException(ErrorCode.NOT_ACCESS_SCHEDULE);
        }

        LocalDateTime startDate;
        LocalDateTime endDate;
        try {
            startDate = LocalDateTime.parse(scheduleRequestDto.getStartDate(), FORMATTER);
            endDate = LocalDateTime.parse(scheduleRequestDto.getEndDate(), FORMATTER);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_SCHEDULE_REQUEST);
        }

        Schedule updatedSchedule = existingSchedule.toBuilder()
                .title(scheduleRequestDto.getTitle())
                .description(scheduleRequestDto.getDescription())
                .status(scheduleRequestDto.getStatus())
                .startDate(startDate)
                .endDate(endDate)
                .importance(scheduleRequestDto.getImportance())
                .build();

        existingSchedule.updateFields(updatedSchedule);
        scheduleRepository.save(existingSchedule);
        return new ScheduleResponseDto(existingSchedule);
    }

    // 일정 삭제
    public void deleteSchedule(Long id) {
        SecurityUser securityUser = getLoggedInUser();
        Long userId = securityUser.getMember().getId();

        Schedule existingSchedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (!existingSchedule.getMember().getId().equals(userId)) {
            throw new CustomException(ErrorCode.NOT_ACCESS_SCHEDULE);
        }

        scheduleRepository.deleteById(id);
    }

    // 전체 SecurityUser 객체를 가져오는 메서드
    private SecurityUser getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (SecurityUser) authentication.getPrincipal();
    }
}