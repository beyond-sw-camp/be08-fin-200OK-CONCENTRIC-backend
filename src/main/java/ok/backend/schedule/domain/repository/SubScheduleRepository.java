package ok.backend.schedule.domain.repository;

import ok.backend.schedule.domain.entity.SubSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SubScheduleRepository extends JpaRepository<SubSchedule, Long> {
    List<SubSchedule> findByScheduleId(Long scheduleId);

    List<SubSchedule> findByScheduleIdIn(List<Long> scheduleIds);
}
