package ok.backend.schedule.service;

import ok.backend.common.exception.CustomException;
import ok.backend.common.exception.ErrorCode;
import ok.backend.common.security.util.SecurityUserDetailService;
import ok.backend.member.domain.entity.Member;
import ok.backend.notification.service.NotificationPendingService;
import ok.backend.notification.service.NotificationService;
import ok.backend.schedule.domain.entity.Schedule;
import ok.backend.schedule.domain.repository.ScheduleRepository;
import ok.backend.schedule.dto.req.ScheduleRequestDto;
import ok.backend.schedule.dto.res.ScheduleResponseDto;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final NotificationService notificationService;
    private final SecurityUserDetailService securityUserDetailService;
    private final NotificationPendingService notificationPendingService;

    public ScheduleService(ScheduleRepository scheduleRepository,
                           NotificationService notificationService,
                           SecurityUserDetailService securityUserDetailService, NotificationPendingService notificationPendingService) {
        this.scheduleRepository = scheduleRepository;
        this.notificationService = notificationService;
        this.securityUserDetailService = securityUserDetailService;
        this.notificationPendingService = notificationPendingService;
    }

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // 모든 일정 조회
    public List<ScheduleResponseDto> getSchedulesForLoggedInUser() {
        Member loggedInMember = securityUserDetailService.getLoggedInMember();
        Long userId = loggedInMember.getId();

        List<Schedule> schedules = scheduleRepository.findByMemberId(userId);
        return schedules.stream().map(ScheduleResponseDto::new).collect(Collectors.toList());
    }

    // 일정 생성
    public ScheduleResponseDto createSchedule(ScheduleRequestDto scheduleRequestDto) {
        Member loggedInMember = securityUserDetailService.getLoggedInMember();

        LocalDateTime startDate;
        LocalDateTime endDate;
        try {
            startDate = LocalDateTime.parse(scheduleRequestDto.getStartDate(), FORMATTER);
            endDate = LocalDateTime.parse(scheduleRequestDto.getEndDate(), FORMATTER);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_SCHEDULE_REQUEST);
        }

        Schedule schedule = Schedule.builder()
                .member(loggedInMember)
                .title(scheduleRequestDto.getTitle())
                .description(scheduleRequestDto.getDescription())
                .status(scheduleRequestDto.getStatus())
                .startDate(startDate)
                .endDate(endDate)
                .importance(scheduleRequestDto.getImportance())
                .startNotification(scheduleRequestDto.isStartNotification())
                .endNotification(scheduleRequestDto.isEndNotification())
                .build();

        scheduleRepository.save(schedule);

        notificationPendingService.saveScheduleToPending(schedule);

        return new ScheduleResponseDto(schedule);
    }

    // 일정 수정
    public ScheduleResponseDto updateSchedule(Long id, ScheduleRequestDto scheduleRequestDto) {
        Member loggedInMember = securityUserDetailService.getLoggedInMember();
        Long userId = loggedInMember.getId();

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
                .startNotification(scheduleRequestDto.isStartNotification())
                .endNotification(scheduleRequestDto.isEndNotification())
                .build();

        existingSchedule.updateFields(updatedSchedule);
        scheduleRepository.save(existingSchedule);
        return new ScheduleResponseDto(existingSchedule);
    }

    // 일정 삭제
    public void deleteSchedule(Long id) {
        Member loggedInMember = securityUserDetailService.getLoggedInMember();
        Long userId = loggedInMember.getId();

        Schedule existingSchedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (!existingSchedule.getMember().getId().equals(userId)) {
            throw new CustomException(ErrorCode.NOT_ACCESS_SCHEDULE);
        }

        scheduleRepository.deleteById(id);
    }
}