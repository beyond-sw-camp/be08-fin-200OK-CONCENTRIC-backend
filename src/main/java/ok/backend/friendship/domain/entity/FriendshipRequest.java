package ok.backend.friendship.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import ok.backend.member.domain.entity.Member;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "friendship_request")
public class FriendshipRequest {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Member member;

    @Column(nullable = false)
    private Long receiverId;
}
