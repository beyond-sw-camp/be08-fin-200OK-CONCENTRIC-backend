package ok.backend.friendship.domain.repository;

import ok.backend.friendship.domain.entity.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    Optional<Friendship> findByMemberIdAndOtherId(Long memberId, Long otherId);
}
