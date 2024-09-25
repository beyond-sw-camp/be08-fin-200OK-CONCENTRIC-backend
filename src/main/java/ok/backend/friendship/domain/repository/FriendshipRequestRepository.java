package ok.backend.friendship.domain.repository;

import ok.backend.friendship.domain.entity.FriendshipRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRequestRepository extends JpaRepository<FriendshipRequest, Long> {

    void deleteByMemberIdAndReceiverId(Long memberId, Long receiverId);
}
