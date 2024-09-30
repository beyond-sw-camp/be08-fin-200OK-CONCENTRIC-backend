package ok.backend.schedule.domain.repository;

import ok.backend.schedule.domain.entity.TeamSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamScheduleRepository extends JpaRepository<TeamSchedule, Long> {
    // 한 팀의 일정 조회
    List<TeamSchedule> findByTeamId(Long teamId);

    // 여러 팀의 일정을 한 번에 조회
    List<TeamSchedule> findByTeamIdIn(List<Long> teamIds);
}

