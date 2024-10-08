package ok.backend.schedule.service;

import ok.backend.common.exception.CustomException;
import ok.backend.common.exception.ErrorCode;
import ok.backend.common.security.util.SecurityUserDetailService;
import ok.backend.notification.service.NotificationPendingService;
import ok.backend.notification.service.NotificationService;
import ok.backend.schedule.domain.entity.Schedule;
import ok.backend.schedule.domain.enums.Status;
import ok.backend.schedule.domain.repository.ScheduleRepository;
import ok.backend.schedule.dto.req.ScheduleRequestDto;
import ok.backend.schedule.dto.res.ScheduleResponseDto;
import ok.backend.member.service.MemberService;
import org.springframework.context.annotation.Lazy;
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
    private final RoutineService routineService;
    private final SubScheduleService subScheduleService;
    private final SecurityUserDetailService securityUserDetailService;
    private final NotificationPendingService notificationPendingService;

    public ScheduleService(ScheduleRepository scheduleRepository,
                           NotificationService notificationService,
                           @Lazy SubScheduleService subScheduleService,
                           RoutineService routineService,
                           MemberService memberService,
                           SecurityUserDetailService securityUserDetailService,
                           NotificationPendingService notificationPendingService) {
        this.scheduleRepository = scheduleRepository;
        this.notificationService = notificationService;
        this.subScheduleService = subScheduleService;
        this.routineService = routineService;
        this.memberService = memberService;
        this.securityUserDetailService = securityUserDetailService;
        this.notificationPendingService = notificationPendingService;
    }

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // 스케줄 ID로 스케줄 엔티티 조회
    @Transactional(readOnly = true)
    public Schedule getScheduleEntityById(Long scheduleId) {
        Long loggedInUserId = securityUserDetailService.getLoggedInMember().getId();
        memberService.findMemberById(loggedInUserId); // 존재 및 활성화된 사용자 확인

        // 스케줄 조회
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));

        // 스케줄의 소유자가 로그인한 사용자인지 확인 (권한 확인)
        if (!schedule.getMember().getId().equals(loggedInUserId)) {
            throw new CustomException(ErrorCode.NOT_ACCESS_SCHEDULE);
        }

        return schedule;
    }

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

        // 해당 일정에 반복 일정이 있는지 확인
        boolean hasRoutine = routineService.existsByScheduleId(existingSchedule.getId());
        if (hasRoutine) {
            // 반복 일정이 있으면 시작일과 종료일이 수정되지 않도록 방지
            if (!existingSchedule.getStartDate().equals(LocalDateTime.parse(scheduleRequestDto.getStartDate())) ||
                    !existingSchedule.getEndDate().equals(LocalDateTime.parse(scheduleRequestDto.getEndDate()))) {
                throw new CustomException(ErrorCode.INVALID_SCHEDULE_REQUEST);
            }
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

    // 완료율 계산 로직
    @Transactional
    public double calculateCompletionRate(Long scheduleId) {
        Schedule schedule = getScheduleEntityById(scheduleId);

        // SubScheduleService를 통해 하위 일정 목록을 가져옴
        var subSchedules = subScheduleService.getSubSchedulesByScheduleEntity(scheduleId); // SubSchedule 엔티티 사용하지 않고 서비스 사용

        if (subSchedules.isEmpty()) {
            return schedule.getStatus() == Status.COMPLETED ? 100.0 : 0.0;
        } else {
            long completedSubSchedules = subSchedules.stream()
                    .filter(subSchedule -> subSchedule.getStatus() == Status.COMPLETED)
                    .count();
            return (completedSubSchedules / (double) subSchedules.size()) * 100.0;
        }
    }

    // 완료율 업데이트 로직
    @Transactional
    public void updateCompletionRate(Long scheduleId) {
        Schedule schedule = getScheduleEntityById(scheduleId);
        double completionRate = calculateCompletionRate(scheduleId);

        // 완료율에 따라 상태를 업데이트
        Status newStatus;
        if (completionRate == 100.0) {
            newStatus = Status.COMPLETED;
        } else if (completionRate > 0.0) {
            newStatus = Status.ACTIVE;
        } else {
            newStatus = Status.INACTIVE;
        }

        // 새로운 상태로 Schedule 엔티티를 빌드하여 업데이트
        Schedule updatedSchedule = schedule.toBuilder()
                .status(newStatus)  // 상태 업데이트
                .build();

        // 업데이트된 객체 저장
        scheduleRepository.save(updatedSchedule);
    }
}