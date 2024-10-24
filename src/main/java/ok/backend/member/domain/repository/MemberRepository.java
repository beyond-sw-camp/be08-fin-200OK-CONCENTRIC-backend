package ok.backend.member.domain.repository;

import ok.backend.member.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findByNickname(String nickname);

    Optional<Member> findByNicknameAndIsActiveTrue(String nickname);

    @Query("select m from Member m where m.id in :memberIdList and m.isActive = true")
    List<Member> findByIdAndIsActiveTrue(@Param("memberIdList") List<Long> memberIdList);
}
