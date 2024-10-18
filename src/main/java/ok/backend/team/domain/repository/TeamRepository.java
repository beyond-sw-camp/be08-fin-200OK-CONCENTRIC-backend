package ok.backend.team.domain.repository;

import ok.backend.member.domain.entity.Member;
import ok.backend.team.domain.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface TeamRepository extends JpaRepository<Team, Long> {
    boolean existsByName(String name);


    @Query("SELECT t FROM Team t JOIN t.teamList tl JOIN tl.member m WHERE m.id = :memberId")
    List<Team> findAllByMemberId(Long memberId);

    @Query("SELECT m FROM Member m JOIN m.teamList tl JOIN tl.team t WHERE t.id = :teamId\n")
    List<Member> getTeamMembers(Long teamId);
}
