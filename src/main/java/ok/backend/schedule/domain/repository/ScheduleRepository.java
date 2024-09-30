package ok.backend.schedule.domain.repository;

import ok.backend.schedule.domain.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByMemberId(Long memberId);
}