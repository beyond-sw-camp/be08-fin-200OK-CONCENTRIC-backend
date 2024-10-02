package ok.backend.schedule.service;

import ok.backend.common.exception.CustomException;
import ok.backend.common.exception.ErrorCode;
import ok.backend.common.security.util.SecurityUserDetailService;
import ok.backend.notification.service.NotificationPendingService;
import ok.backend.notification.service.NotificationService;
import ok.backend.schedule.domain.entity.Schedule;
import ok.backend.schedule.domain.repository.ScheduleRepository;
import ok.backend.schedule.dto.req.ScheduleRequestDto;
import ok.backend.schedule.dto.res.ScheduleResponseDto;
import ok.backend.member.service.MemberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final NotificationService notificationService;
    private final MemberService memberService;
    private final SecurityUserDetailService securityUserDetailService;
    private final NotificationPendingService notificationPendingService;

    public ScheduleService(ScheduleRepository scheduleRepository,
                           NotificationService notificationService,
                           MemberService memberService,
                           SecurityUserDetailService securityUserDetailService,
                           NotificationPendingService notificationPendingService) {
        this.scheduleRepository = scheduleRepository;
        this.notificationService = notificationService;
        this.memberService = memberService;
        this.securityUserDetailService = securityUserDetailService;
        this.notificationPendingService = notificationPendingService;
    }

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // 특정 일정 조회
    @Transactional(readOnly = true)
    public ScheduleResponseDto getScheduleById(Long scheduleId) {
        Long loggedInUserId = securityUserDetailService.getLoggedInMember().getId();
        memberService.findMemberById(loggedInUserId); // 존재 및 활성화된 사용자 확인

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (!schedule.getMember().getId().equals(loggedInUserId)) {
            throw new CustomException(ErrorCode.NOT_ACCESS_SCHEDULE);
        }

        return new ScheduleResponseDto(schedule);
    }

    // 모든 일정 조회
    public List<ScheduleResponseDto> getSchedulesForLoggedInUser() {
        Long loggedInUserId = securityUserDetailService.getLoggedInMember().getId();
        memberService.findMemberById(loggedInUserId); // 존재 및 활성화된 사용자 확인

        List<Schedule> schedules = scheduleRepository.findByMemberId(loggedInUserId);
        return schedules.stream().map(ScheduleResponseDto::new).collect(Collectors.toList());
    }

    // 일정 생성
    @Transactional
    public ScheduleResponseDto createSchedule(ScheduleRequestDto scheduleRequestDto) {
        Long loggedInUserId = securityUserDetailService.getLoggedInMember().getId();
        memberService.findMemberById(loggedInUserId); // 존재 및 활성화된 사용자 확인

        LocalDateTime startDate;
        LocalDateTime endDate;
        try {
            startDate = LocalDateTime.parse(scheduleRequestDto.getStartDate(), FORMATTER);
            endDate = LocalDateTime.parse(scheduleRequestDto.getEndDate(), FORMATTER);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_SCHEDULE_REQUEST);
        }

        Schedule schedule = Schedule.builder()
                .member(securityUserDetailService.getLoggedInMember())  // SecurityUserDetailService에서 회원 정보 가져오기
                .title(scheduleRequestDto.getTitle())
                .description(scheduleRequestDto.getDescription())
                .status(scheduleRequestDto.getStatus())
                .startDate(startDate)
                .endDate(endDate)
                .importance(scheduleRequestDto.getImportance())
                .startNotification(scheduleRequestDto.getStartNotification())
                .endNotification(scheduleRequestDto.getEndNotification())
                .build();

        scheduleRepository.save(schedule);

        notificationPendingService.saveScheduleToPending(schedule);

        return new ScheduleResponseDto(schedule);
    }

    // 일정 수정
    @Transactional
    public ScheduleResponseDto updateSchedule(Long id, ScheduleRequestDto scheduleRequestDto) {
        Long loggedInUserId = securityUserDetailService.getLoggedInMember().getId();
        memberService.findMemberById(loggedInUserId); // 존재 및 활성화된 사용자 확인

        Schedule existingSchedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (!existingSchedule.getMember().getId().equals(loggedInUserId)) {
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
                .startNotification(scheduleRequestDto.getStartNotification())
                .endNotification(scheduleRequestDto.getEndNotification())
                .build();

        notificationPendingService.updateScheduleToPending(existingSchedule, updatedSchedule);

        existingSchedule.updateFields(updatedSchedule);
        scheduleRepository.save(existingSchedule);


        return new ScheduleResponseDto(existingSchedule);
    }

    // 일정 삭제
    @Transactional
    public void deleteSchedule(Long id) {
        Long loggedInUserId = securityUserDetailService.getLoggedInMember().getId();
        memberService.findMemberById(loggedInUserId); // 존재 및 활성화된 사용자 확인

        Schedule existingSchedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (!existingSchedule.getMember().getId().equals(loggedInUserId)) {
            throw new CustomException(ErrorCode.NOT_ACCESS_SCHEDULE);
        }

        scheduleRepository.deleteById(id);
    }
}