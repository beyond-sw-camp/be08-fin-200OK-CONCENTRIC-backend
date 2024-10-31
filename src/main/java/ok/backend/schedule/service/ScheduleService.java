package ok.backend.schedule.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ok.backend.common.exception.CustomException;
import ok.backend.common.exception.ErrorCode;
import ok.backend.common.security.util.SecurityUserDetailService;
import ok.backend.member.domain.entity.Member;
import ok.backend.notification.service.NotificationPendingService;
import ok.backend.notification.service.NotificationService;
import ok.backend.schedule.domain.entity.Schedule;
import ok.backend.schedule.domain.enums.Status;
import ok.backend.schedule.domain.enums.Type;
import ok.backend.schedule.domain.repository.ScheduleRepository;
import ok.backend.schedule.dto.req.ScheduleRequestDto;
import ok.backend.schedule.dto.res.ScheduleResponseDto;
import ok.backend.member.service.MemberService;
import ok.backend.schedule.dto.res.SubScheduleResponseDto;
import ok.backend.team.domain.entity.TeamList;
import ok.backend.team.service.TeamService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final NotificationService notificationService;
    private final MemberService memberService;
    private final SubScheduleService subScheduleService;
    private final SecurityUserDetailService securityUserDetailService;
    private final NotificationPendingService notificationPendingService;
    private final TeamService teamService;

    // 일정 생성
    public ScheduleResponseDto createSchedule(ScheduleRequestDto scheduleRequestDto) {
        Member member = memberService.findMemberById(securityUserDetailService.getLoggedInMember().getId());

        Schedule schedule = Schedule.builder()
                .member(member)  // SecurityUserDetailService에서 회원 정보 가져오기
                .title(scheduleRequestDto.getTitle())
                .description(scheduleRequestDto.getDescription())
                .status(scheduleRequestDto.getStatus())
                .startDate(scheduleRequestDto.getStartDate())
                .endDate(scheduleRequestDto.getEndDate())
                .importance(scheduleRequestDto.getImportance())
                .type(scheduleRequestDto.getType())
                .teamId(scheduleRequestDto.getTeamId())
                .build();

        // 팀 일정일 경우 팀 가입 여부 확인
        if (schedule.getType().equals(Type.TEAM)) {
            List<TeamList> teamList = teamService.findByTeamId(schedule.getTeamId());
            boolean memberExists = teamList.stream()
                    .anyMatch(team -> team.getMember().equals(member));

            if (!memberExists) {
                throw new CustomException(ErrorCode.TEAM_NOT_FOUND);
            }
        }
        scheduleRepository.save(schedule);

//        notificationPendingService.saveScheduleToPending(schedule);

        return new ScheduleResponseDto(schedule);
    }

    // 일정 수정
    public ScheduleResponseDto updateSchedule(ScheduleRequestDto scheduleRequestDto) {
        Member member = memberService.findMemberById(securityUserDetailService.getLoggedInMember().getId());

        Schedule schedule = scheduleRepository.findById(member.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (!schedule.getMember().getId().equals(member.getId())) {
            throw new CustomException(ErrorCode.NOT_ACCESS_SCHEDULE);
        }

        schedule.updateSchedule(scheduleRequestDto);
        schedule.updateScheduleUpdateAt();

        Schedule updatedSchedule = scheduleRepository.save(schedule);

//        notificationPendingService.updateScheduleToPending(schedule, updatedSchedule);

        return new ScheduleResponseDto(updatedSchedule);
    }

    // 모든 일정 조회(전체 일정)
    public List<ScheduleResponseDto> findAllSchedules() {
        Member member = memberService.findMemberById(securityUserDetailService.getLoggedInMember().getId());

        // 개인 일정 조회
        List<Schedule> privateSchedules = scheduleRepository.findByMemberId(member.getId())
                .stream()
                .filter(schedule -> schedule.getType().equals(Type.PRIVATE))
                .toList();
        List<Schedule> allSchedules = new ArrayList<>(privateSchedules);

        // 가입한 팀 일정 조회
        List<TeamList> teamList = teamService.findByMemberId(member.getId());
        for (TeamList team : teamList) {
            List<Schedule> teamSchedules = scheduleRepository.findByTeamId(team.getId());
            allSchedules.addAll(teamSchedules);
        }

        return allSchedules.stream().map(ScheduleResponseDto::new).collect(Collectors.toList());
    }

    // 특정 팀 일정 조회
    public List<ScheduleResponseDto> findAllTeamSchedules(Long teamId) {
        List<Schedule> teamSchedules = scheduleRepository.findByTeamId(teamId);
        return teamSchedules.stream().map(ScheduleResponseDto::new).collect(Collectors.toList());
    }

    // 일정 상세 조회
    public ScheduleResponseDto findScheduleById(Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow(()
                -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));
        return new ScheduleResponseDto(schedule);
    }

    // 일정 삭제
    public void deleteSchdule(Long scheduleId) {
        Member member = memberService.findMemberById(securityUserDetailService.getLoggedInMember().getId());
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (!schedule.getMember().getId().equals(member.getId())) {
            throw new CustomException(ErrorCode.NOT_ACCESS_SCHEDULE);
        }
        scheduleRepository.delete(schedule);
    }

    // 상태 업데이트
    public void updateScheduleStatus(Long scheduleId, Status status) {
        Member member = memberService.findMemberById(securityUserDetailService.getLoggedInMember().getId());
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (!schedule.getMember().getId().equals(member.getId())) {
            throw new CustomException(ErrorCode.NOT_ACCESS_SCHEDULE);
        }
        schedule.updateScheduleStatus(status);
        scheduleRepository.save(schedule);
    }

    // 진도율 계산
    public void calculateProgress(Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));

        List<SubScheduleResponseDto> subSchedules = subScheduleService.getSubSchedulesByScheduleId(scheduleId);

        // 일정이 INACTIVE 상태인 경우 상태를 변경하지 않고 그대로 유지
        if (schedule.getStatus() == Status.INACTIVE) {
            return;
        }

        if (subSchedules.isEmpty()) {
            // 하위 일정이 없고 일정이 완료 상태라면 진행률 100%, 그렇지 않으면 0%
            schedule = schedule.toBuilder()
                    .progress(schedule.getStatus() == Status.COMPLETED ? 100 : 0)
                    .status(schedule.getStatus() == Status.COMPLETED ? Status.COMPLETED : Status.ACTIVE)
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