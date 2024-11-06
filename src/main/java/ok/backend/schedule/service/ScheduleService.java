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
import ok.backend.schedule.domain.entity.SubSchedule;
import ok.backend.schedule.domain.enums.Status;
import ok.backend.schedule.domain.enums.Type;
import ok.backend.schedule.domain.repository.ScheduleRepository;
import ok.backend.schedule.domain.repository.SubScheduleRepository;
import ok.backend.schedule.dto.req.ScheduleRequestDto;
import ok.backend.schedule.dto.res.ScheduleListResponseDto;
import ok.backend.schedule.dto.res.ScheduleResponseDto;
import ok.backend.member.service.MemberService;
import ok.backend.schedule.dto.res.SubScheduleResponseDto;
import ok.backend.team.domain.entity.Team;
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
    private final SecurityUserDetailService securityUserDetailService;
    private final NotificationPendingService notificationPendingService;
    private final TeamService teamService;
    private final SubScheduleRepository subScheduleRepository;

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

        if (schedule.getStatus().equals(Status.COMPLETED)) {
            schedule.updateScheduleProgress(100);
        } else schedule.updateScheduleProgress(0);

        scheduleRepository.save(schedule);

        notificationPendingService.saveScheduleToPending(schedule);

        return new ScheduleResponseDto(schedule);
    }

    // 일정 수정
    public ScheduleResponseDto updateSchedule(Long scheduleId, ScheduleRequestDto scheduleRequestDto) {
        Member member = memberService.findMemberById(securityUserDetailService.getLoggedInMember().getId());

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (!schedule.getMember().getId().equals(member.getId())) {
            throw new CustomException(ErrorCode.NOT_ACCESS_SCHEDULE);
        }

        schedule.updateSchedule(scheduleRequestDto);
        schedule.updateScheduleUpdateAt();

        Schedule updatedSchedule = scheduleRepository.save(schedule);
        calculateProgress(updatedSchedule.getId());

        notificationPendingService.updateScheduleToPending(schedule, updatedSchedule);

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
    public ScheduleListResponseDto findScheduleById(Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow(()
                -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));
        if (schedule.getType().equals(Type.TEAM)) {
            Team team = teamService.findById(schedule.getTeamId());
            return new ScheduleListResponseDto(schedule, team.getName());
        } else {
            return new ScheduleListResponseDto(schedule, null);
        }
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
        calculateProgress(schedule.getId());
        scheduleRepository.save(schedule);
    }

    // 진행도 계산
    public void calculateProgress(Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));
        List<SubSchedule> subSchedules = subScheduleRepository.findByScheduleId(scheduleId);

//        if (schedule.getStatus().equals(Status.COMPLETED)) {
//            schedule.updateScheduleProgress(100);
//            scheduleRepository.save(schedule);
//        } else if (subSchedules.isEmpty()) {
//            schedule.updateScheduleProgress(
//                    (schedule.getStatus().equals(Status.COMPLETED) ? 100 : 0)
//            );
//            scheduleRepository.save(schedule);
//        } else {
//            int count = (int) subSchedules.stream()
//                    .filter(subSchedule -> subSchedule.getStatus().equals(Status.COMPLETED))
//                    .count();
//            System.out.println(count);
//
//            int progress = count * 100 / subSchedules.size();
//            schedule.updateScheduleProgress(progress);
//            System.out.println(progress);
//            if (progress == 100) {
//                schedule.updateScheduleStatus(Status.COMPLETED);
//            }
//            scheduleRepository.save(schedule);
//        }
        if (!subSchedules.isEmpty()) {
            int count = (int) subSchedules.stream()
                    .filter(subSchedule -> subSchedule.getStatus().equals(Status.COMPLETED))
                    .count();
            System.out.println(count);

            int progress = count * 100 / subSchedules.size();
            schedule.updateScheduleProgress(progress);
            System.out.println(progress);

            if (progress == 100) {
                schedule.updateScheduleStatus(Status.COMPLETED);
            } else {
                schedule.updateScheduleStatus(Status.ACTIVE);
            }
            scheduleRepository.save(schedule);
        } else {
            schedule.updateScheduleProgress(
                    (schedule.getStatus().equals(Status.COMPLETED) ? 100 : 0)
            );
            scheduleRepository.save(schedule);
        }
    }
}