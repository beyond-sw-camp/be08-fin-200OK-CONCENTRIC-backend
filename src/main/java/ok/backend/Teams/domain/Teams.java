package ok.backend.Teams.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Table(name = "teams")
public class Teams {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatroom;

    @Column(nullable = false, length = 10)
    private String name;

    @Column(name = "creator_id", nullable = false)
    private Long creator_id;

    @CreationTimestamp
    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt;

    @OneToMany(mappedBy = "teams")
    private List<TeamList> teamList = new ArrayList<>();

    @OneToMany(mappedBy = "teams")
    private List<TeamSchedule> teamSchedule = new ArrayList<>();

}