package ok.backend.friendship.domain.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import ok.backend.member.domain.entity.Member;
import org.springframework.stereotype.Repository;

import java.util.List;

import static ok.backend.friendship.domain.entity.QFriendship.friendship;
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
                .where(friendship.member.id.eq(memberId))
                .fetch();
    }
}
