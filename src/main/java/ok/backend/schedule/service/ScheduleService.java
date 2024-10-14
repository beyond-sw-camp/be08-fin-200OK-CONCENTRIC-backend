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
import ok.backend.schedule.dto.res.SubScheduleResponseDto;
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

        calculateProgress(schedule.getId());
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

        calculateProgress(updatedSchedule.getId());

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

    @Transactional
    public void calculateProgress(Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));

        List<SubScheduleResponseDto> subSchedules = subScheduleService.getSubSchedulesByScheduleId(scheduleId);

        if (subSchedules.isEmpty()) {
            // 하위 일정이 없고 일정이 완료 상태라면 진행률 100%, 그렇지 않으면 0%
            schedule = schedule.toBuilder()
                    .progress(schedule.getStatus() == Status.COMPLETED ? 100 : 0)
                    .status(schedule.getProgress() < 100 ? Status.ACTIVE : schedule.getStatus())
                    .build();
        } else {
            // 완료된 하위 일정의 비율 계산
            long completedCount = subSchedules.stream()
                    .filter(subSchedule -> subSchedule.getStatus() == Status.COMPLETED)
                    .count();
            int progress = (int) ((double) completedCount / subSchedules.size() * 100);

            // 모든 하위 일정이 완료되었을 경우 상위 일정의 상태를 COMPLETED로 변경
            Status newStatus = (completedCount == subSchedules.size()) ? Status.COMPLETED : Status.ACTIVE;

            // 기존에 COMPLETED였으나 하위 일정이 추가되면 ACTIVE로 변경
            if (schedule.getStatus() == Status.COMPLETED && newStatus == Status.ACTIVE) {
                newStatus = Status.ACTIVE;
            }

            schedule = schedule.toBuilder()
                    .progress(progress)
                    .status(newStatus)
                    .build();
        }

        scheduleRepository.save(schedule);
    }
}