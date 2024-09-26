package ok.backend.friendship.domain.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import ok.backend.friendship.domain.entity.FriendshipRequest;
import ok.backend.friendship.domain.enums.FriendshipRequestStatus;
import ok.backend.member.domain.entity.Member;
import ok.backend.member.domain.enums.MemberStatus;
import org.springframework.stereotype.Repository;

import java.util.List;

import static ok.backend.friendship.domain.entity.QFriendship.friendship;
import static ok.backend.friendship.domain.entity.QFriendshipRequest.friendshipRequest;
import static ok.backend.member.domain.entity.QMember.member;

@Repository
@RequiredArgsConstructor
public class FriendshipCustomRepositoryImpl implements FriendshipCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Member> findMembersByMemberId(Long memberId) {
        return jpaQueryFactory.select(member)
                .from(member)
                .innerJoin(friendship)
                .on(member.id.eq(friendship.otherId))
                .where(friendship.member.id.eq(memberId), member.status.eq(MemberStatus.Y))
                .fetch();
    }

//    SELECT *
//    FROM users u
//    INNER JOIN friendship f ON u.id = f.other_id
//    WHERE f.user_id = 1;

    @Override
    public List<FriendshipRequest> findFriendshipRequestsByReceiverId(Long receiverId) {
        return jpaQueryFactory.select(friendshipRequest)
                .from(friendshipRequest)
                .innerJoin(friendshipRequest.member, member).fetchJoin()
                .where(friendshipRequest.receiverId.eq(receiverId),
                        member.status.eq(MemberStatus.Y),
                        friendshipRequest.status.eq(FriendshipRequestStatus.WAITING))
                .fetch();
    }

//    SELECT *
//    FROM friendship_request fr
//    INNER JOIN users u ON fr.user_id = u.id
//    WHERE fr.receiver_id = 2 AND u.status = 'Y' AND fr.status = 'WAITING';
}
