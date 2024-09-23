package ok.backend.Teams.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
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

    @Column(nullable = false)
    private Long creator_id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Member member;

    @CreationTimestamp
    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt;

    @OneToMany(mappedBy = "users")
    private List<TeamList> teamList = new ArrayList<>();

}