package ok.backend.schedule.domain.repository;

import ok.backend.schedule.domain.entity.Schedule;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    @EntityGraph(attributePaths = {"routine", "teamSchedule"})
    List<Schedule> findByMemberId(Long memberId);
}