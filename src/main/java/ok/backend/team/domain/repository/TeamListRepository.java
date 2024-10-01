package ok.backend.team.domain.repository;

import ok.backend.member.domain.entity.Member;
import ok.backend.team.domain.entity.TeamList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamListRepository extends JpaRepository<TeamList, Long> {
    List<TeamList> findByMemberId(Long memberId);

    boolean existsByTeamIdAndMemberId(Long teamId, Long memberId);

    List<TeamList> findByMemberIdAndTeamId(Long memberId, Long teamId);

    List<TeamList> findByTeamId(Long teamId);
}
