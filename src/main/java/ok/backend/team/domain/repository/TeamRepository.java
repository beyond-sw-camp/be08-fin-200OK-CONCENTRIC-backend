package ok.backend.team.domain.repository;

import ok.backend.team.domain.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface TeamRepository extends JpaRepository<Team, Long> {
    boolean existsByName(String name);


    @Query("SELECT t FROM Team t JOIN t.teamList tl JOIN tl.member m WHERE m.id = :memberId")
    List<Team> findAllByMemberId(Long memberId);
}
