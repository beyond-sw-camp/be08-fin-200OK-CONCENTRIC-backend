package ok.backend.schedule.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ok.backend.common.exception.CustomException;
import ok.backend.common.exception.ErrorCode;
import ok.backend.member.domain.entity.Member;
import ok.backend.schedule.domain.entity.Schedule;
import ok.backend.schedule.domain.entity.SubSchedule;
import ok.backend.schedule.domain.enums.Status;
import ok.backend.schedule.domain.repository.ScheduleRepository;
import ok.backend.schedule.domain.repository.SubScheduleRepository;
import ok.backend.schedule.dto.req.SubScheduleRequestDto;
import ok.backend.schedule.dto.res.SubScheduleResponseDto;
import ok.backend.common.security.util.SecurityUserDetailService;
import ok.backend.member.service.MemberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class SubScheduleService {

    private final SubScheduleRepository subScheduleRepository;
    private final ScheduleService scheduleService;
    private final MemberService memberService;
    private final SecurityUserDetailService securityUserDetailService;
    private final ScheduleRepository scheduleRepository;

//    public List<SubSchedule> getSubSchedulesByScheduleEntity(Long scheduleId) {
//        var schedule = scheduleService.getScheduleEntityById(scheduleId); // 상위 일정 확인
//        List<SubSchedule> subSchedules = subScheduleRepository.findByScheduleId(scheduleId); // 하위 일정 조회
//        return subSchedules;
//    }

    // 하위 일정 생성
    public SubScheduleResponseDto createSubSchedule(SubScheduleRequestDto subScheduleRequestDto) {
        Member member = memberService.findMemberById(securityUserDetailService.getLoggedInMember().getId());

        Schedule schedule = scheduleRepository.findById(subScheduleRequestDto.getScheduleId())
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (!schedule.getMember().equals(member)) {
            throw new CustomException(ErrorCode.NOT_ACCESS_SCHEDULE);
        }

        SubSchedule subSchedule = SubSchedule.builder()
                .schedule(schedule)
                .title(subScheduleRequestDto.getTitle())
                .description(subScheduleRequestDto.getDescription())
                .status(subScheduleRequestDto.getStatus())
                .build();

        scheduleService.calculateProgress(subSchedule.getSchedule().getId());
        subScheduleRepository.save(subSchedule);
        return new SubScheduleResponseDto(subSchedule);
    }

    // 하위 일정 수정
    public SubScheduleResponseDto updateSubSchedule(Long subScheduleId, SubScheduleRequestDto subScheduleRequestDto) {
        Member member = memberService.findMemberById(securityUserDetailService.getLoggedInMember().getId());

        SubSchedule subSchedule = subScheduleRepository.findById(subScheduleId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (!subSchedule.getSchedule().getMember().equals(member)) {
            throw new CustomException(ErrorCode.NOT_ACCESS_SCHEDULE);
        }

        subSchedule.updateSubSchedule(subScheduleRequestDto);
        SubSchedule updatedSubSchedule = subScheduleRepository.save(subSchedule);

        scheduleService.calculateProgress(updatedSubSchedule.getSchedule().getId());
        return new SubScheduleResponseDto(updatedSubSchedule);
    }

    // 하위 일정 상태 업데이트
    public void updateSubScheduleStatus(Long subScheduleId, Status status) {
        Member member = memberService.findMemberById(securityUserDetailService.getLoggedInMember().getId());

        SubSchedule subSchedule = subScheduleRepository.findById(subScheduleId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (!subSchedule.getSchedule().getMember().equals(member)) {
            throw new CustomException(ErrorCode.NOT_ACCESS_SCHEDULE);
        }

        subSchedule.updateSubScheduleStatus(status);
        SubSchedule updatedSubSchedule = subScheduleRepository.save(subSchedule);

        scheduleService.calculateProgress(updatedSubSchedule.getSchedule().getId());
    }

    // 하위 일정 삭제
    public void deleteSubSchedule(Long subScheduleId) {
        Member member = memberService.findMemberById(securityUserDetailService.getLoggedInMember().getId());

        SubSchedule subSchedule = subScheduleRepository.findById(subScheduleId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (!subSchedule.getSchedule().getMember().equals(member)) {
            throw new CustomException(ErrorCode.NOT_ACCESS_SCHEDULE);
        }

        scheduleService.calculateProgress(subSchedule.getSchedule().getId());
        subScheduleRepository.delete(subSchedule);
    }

    // 상위 일정에 따른 하위 일정 조회
    public List<SubScheduleResponseDto> findAllSubSchedulesByScheduleId(Long scheduleId) {
        List<SubSchedule> subSchedules = subScheduleRepository.findByScheduleId(scheduleId);
        return subSchedules.stream()
                .map(SubScheduleResponseDto::new)
                .collect(Collectors.toList());
    }
}
