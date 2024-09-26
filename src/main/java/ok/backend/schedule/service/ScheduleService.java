package ok.backend.schedule.service;

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

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public List<ScheduleResponseDto> getSchedulesForLoggedInUser() {
        Long userId = getLoggedInUserId();

        List<Schedule> schedules = scheduleRepository.findByMemberId(userId);
        return schedules.stream().map(ScheduleResponseDto::new).collect(Collectors.toList());
    }

    // 일정 생성
    public ScheduleResponseDto createSchedule(ScheduleRequestDto scheduleRequestDto) {
        Long userId = getLoggedInUserId();
        SecurityUser securityUser = getLoggedInUser();

        LocalDateTime startDate = LocalDateTime.parse(scheduleRequestDto.getStartDate(), FORMATTER);
        LocalDateTime endDate = LocalDateTime.parse(scheduleRequestDto.getEndDate(), FORMATTER);

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
        Long userId = getLoggedInUserId();
        Schedule existingSchedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        if (!existingSchedule.getMember().getId().equals(userId)) {
            throw new RuntimeException("You can only modify your own schedules.");
        }

        LocalDateTime startDate = LocalDateTime.parse(scheduleRequestDto.getStartDate(), FORMATTER);
        LocalDateTime endDate = LocalDateTime.parse(scheduleRequestDto.getEndDate(), FORMATTER);

        // updateAt 때문에 @Setter 사용
        existingSchedule.setTitle(scheduleRequestDto.getTitle());
        existingSchedule.setDescription(scheduleRequestDto.getDescription());
        existingSchedule.setStatus(scheduleRequestDto.getStatus());
        existingSchedule.setStartDate(startDate);
        existingSchedule.setEndDate(endDate);
        existingSchedule.setImportance(scheduleRequestDto.getImportance());

        scheduleRepository.save(existingSchedule);

        return new ScheduleResponseDto(existingSchedule);
    }


    // 일정 삭제
    public void deleteSchedule(Long id) {
        Long userId = getLoggedInUserId();
        Schedule existingSchedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));

        if (!existingSchedule.getMember().getId().equals(userId)) {
            throw new RuntimeException("You can only delete your own schedules.");
        }

        scheduleRepository.deleteById(id);
    }

    private Long getLoggedInUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        return securityUser.getMember().getId();
    }

    private SecurityUser getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (SecurityUser) authentication.getPrincipal();
    }
}
