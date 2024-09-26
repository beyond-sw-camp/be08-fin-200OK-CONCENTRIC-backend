package ok.backend.friendship.domain.repository;

import ok.backend.friendship.domain.entity.FriendshipRequest;
import ok.backend.member.domain.entity.Member;

import java.util.List;

public interface FriendshipCustomRepository {
    List<Member> findMembersByMemberId(Long otherId);

    List<FriendshipRequest> findFriendshipRequestsByReceiverId(Long receiverId);
}
