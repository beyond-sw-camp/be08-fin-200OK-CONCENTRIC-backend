package ok.backend.Teams.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Table(name = "team_list")
public class TeamList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_list_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Teams teams;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Member member;





}
