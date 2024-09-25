package ok.backend.friendship.domain.repository;

import ok.backend.member.domain.entity.Member;

import java.util.List;

public interface FriendshipCustomRepository {
    List<Member> findMembersByMemberId(Long otherId);
}
