package ok.backend.friendship.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ok.backend.member.domain.entity.Member;

@Entity
@Table(name = "friendship")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class Friendship {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Member member;

    @Column(name = "other_id", nullable = false)
    private Long otherId;

}
