package ok.backend.team.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import ok.backend.member.domain.entity.Member;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Table(name = "team_list")
public class TeamList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Member member;




}
