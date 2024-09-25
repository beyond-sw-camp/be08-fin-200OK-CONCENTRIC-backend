package ok.backend.schedule.domain.repository;

import ok.backend.schedule.domain.entity.Routine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoutineRepository extends JpaRepository<Routine, Long> {
}
