package ok.backend.friendship.domain.repository;

import ok.backend.friendship.domain.entity.FriendshipRequest;
import ok.backend.friendship.domain.enums.FriendshipRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRequestRepository extends JpaRepository<FriendshipRequest, Long> {

    Optional<FriendshipRequest> findByMemberIdAndReceiverIdAndStatus(Long memberId, Long receiverId, FriendshipRequestStatus status);

    List<FriendshipRequest> findFriendshipRequestsByReceiverId(Long receiverId);
}
