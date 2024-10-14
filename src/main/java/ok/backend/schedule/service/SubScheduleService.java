package ok.backend.schedule.service;

import ok.backend.common.exception.CustomException;
import ok.backend.common.exception.ErrorCode;
import ok.backend.schedule.domain.entity.SubSchedule;
import ok.backend.schedule.domain.repository.SubScheduleRepository;
import ok.backend.schedule.dto.req.SubScheduleRequestDto;
import ok.backend.schedule.dto.res.SubScheduleResponseDto;
import ok.backend.common.security.util.SecurityUserDetailService;
import ok.backend.member.service.MemberService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static ok.backend.schedule.domain.entity.QSubSchedule.subSchedule;

@Service
public class SubScheduleService {

    private final SubScheduleRepository subScheduleRepository;
    private final ScheduleService scheduleService;
    private final MemberService memberService;
    private final SecurityUserDetailService securityUserDetailService;

    public SubScheduleService(SubScheduleRepository subScheduleRepository,
                              @Lazy ScheduleService scheduleService,
                              MemberService memberService,
                              SecurityUserDetailService securityUserDetailService) {
        this.subScheduleRepository = subScheduleRepository;
        this.scheduleService = scheduleService;
        this.memberService = memberService;
        this.securityUserDetailService = securityUserDetailService;
    }

    public List<SubSchedule> getSubSchedulesByScheduleEntity(Long scheduleId) {
        var schedule = scheduleService.getScheduleEntityById(scheduleId); // 상위 일정 확인
        List<SubSchedule> subSchedules = subScheduleRepository.findByScheduleId(scheduleId); // 하위 일정 조회
        return subSchedules;
    }

    // 하위 일정 조회
    @Transactional(readOnly = true)
    public SubScheduleResponseDto getSubScheduleById(Long subScheduleId) {
        Long loggedInUserId = securityUserDetailService.getLoggedInMember().getId();
        memberService.findMemberById(loggedInUserId);

        SubSchedule subSchedule = subScheduleRepository.findById(subScheduleId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (!subSchedule.getSchedule().getMember().getId().equals(loggedInUserId)) {
            throw new CustomException(ErrorCode.NOT_ACCESS_SCHEDULE);
        }

        return new SubScheduleResponseDto(subSchedule);
    }

    // 상위 일정에 속한 하위 일정 전체 조회
    @Transactional(readOnly = true)
    public List<SubScheduleResponseDto> getSubSchedulesByScheduleId(Long scheduleId) {
        Long loggedInUserId = securityUserDetailService.getLoggedInMember().getId();
        memberService.findMemberById(loggedInUserId);

        var schedule = scheduleService.getScheduleEntityById(scheduleId);

        List<SubSchedule> subSchedules = subScheduleRepository.findByScheduleId(scheduleId);
        return subSchedules.stream()
                .map(SubScheduleResponseDto::new)
                .collect(Collectors.toList());
    }

    // 하위 일정 생성
    @Transactional
    public SubScheduleResponseDto createSubSchedule(SubScheduleRequestDto subScheduleRequestDto) {
        Long loggedInUserId = securityUserDetailService.getLoggedInMember().getId();
        memberService.findMemberById(loggedInUserId); // 존재 및 활성화된 사용자 확인

        // 상위 일정 확인 및 권한 검증
        Long scheduleId = subScheduleRequestDto.getScheduleId();
        var schedule = scheduleService.getScheduleEntityById(scheduleId);
        if (!schedule.getMember().getId().equals(loggedInUserId)) {
            throw new CustomException(ErrorCode.NOT_ACCESS_SCHEDULE);
        }

        // String으로 받은 시작일과 종료일을 LocalDateTime으로 변환
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime startDate = LocalDateTime.parse(subScheduleRequestDto.getStartDate(), formatter);
        LocalDateTime endDate = LocalDateTime.parse(subScheduleRequestDto.getEndDate(), formatter);

        // 하위 일정의 시작일과 종료일이 상위 일정의 범위 내에 있는지 확인
        if (startDate.isBefore(schedule.getStartDate()) || endDate.isAfter(schedule.getEndDate())) {
            throw new CustomException(ErrorCode.INVALID_SCHEDULE_REQUEST);
        }

        SubSchedule subSchedule = SubSchedule.builder()
                .schedule(schedule)
                .title(subScheduleRequestDto.getTitle())
                .description(subScheduleRequestDto.getDescription())
                .status(subScheduleRequestDto.getStatus())
                .startDate(startDate)
                .endDate(endDate)
                .build();

        scheduleService.calculateProgress(subSchedule.getSchedule().getId());
        subScheduleRepository.save(subSchedule);
        return new SubScheduleResponseDto(subSchedule);
    }

    // 하위 일정 수정
    @Transactional
    public SubScheduleResponseDto updateSubSchedule(Long subScheduleId, SubScheduleRequestDto subScheduleRequestDto) {
        Long loggedInUserId = securityUserDetailService.getLoggedInMember().getId();
        memberService.findMemberById(loggedInUserId);

        SubSchedule existingSubSchedule = subScheduleRepository.findById(subScheduleId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (!existingSubSchedule.getSchedule().getMember().getId().equals(loggedInUserId)) {
            throw new CustomException(ErrorCode.NOT_ACCESS_SCHEDULE);
        }

        var schedule = scheduleService.getScheduleEntityById(existingSubSchedule.getSchedule().getId());

        // 하위 일정의 시작일과 종료일이 상위 일정의 시작일과 종료일 내에 있는지 확인
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime startDate = LocalDateTime.parse(subScheduleRequestDto.getStartDate(), formatter);
        LocalDateTime endDate = LocalDateTime.parse(subScheduleRequestDto.getEndDate(), formatter);
        if (startDate.isBefore(schedule.getStartDate()) || endDate.isAfter(schedule.getEndDate())) {
            throw new CustomException(ErrorCode.INVALID_SCHEDULE_REQUEST);
        }

        SubSchedule updatedSubSchedule = existingSubSchedule.toBuilder()
                .title(subScheduleRequestDto.getTitle())
                .description(subScheduleRequestDto.getDescription())
                .status(subScheduleRequestDto.getStatus())
                .startDate(startDate)
                .endDate(endDate)
                .build();

        scheduleService.calculateProgress(updatedSubSchedule.getSchedule().getId());
        subScheduleRepository.save(updatedSubSchedule);
        return new SubScheduleResponseDto(updatedSubSchedule);
    }

    // 하위 일정 삭제
    @Transactional
    public void deleteSubSchedule(Long subScheduleId) {
        Long loggedInUserId = securityUserDetailService.getLoggedInMember().getId();
        memberService.findMemberById(loggedInUserId);

        SubSchedule subSchedule = subScheduleRepository.findById(subScheduleId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (!subSchedule.getSchedule().getMember().getId().equals(loggedInUserId)) {
            throw new CustomException(ErrorCode.NOT_ACCESS_SCHEDULE);
        }

        scheduleService.calculateProgress(subSchedule.getSchedule().getId());
        subScheduleRepository.deleteById(subScheduleId);
    }
}
