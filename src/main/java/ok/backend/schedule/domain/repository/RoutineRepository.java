package ok.backend.schedule.domain.repository;

import ok.backend.schedule.domain.entity.Routine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoutineRepository extends JpaRepository<Routine, Long> {
    List<Routine> findBySchedule_MemberId(Long memberId);
}
