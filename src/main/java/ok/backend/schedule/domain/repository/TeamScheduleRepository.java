package ok.backend.schedule.domain.repository;

import ok.backend.schedule.domain.entity.TeamSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamScheduleRepository extends JpaRepository<TeamSchedule, Long> {
}
