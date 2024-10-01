package ok.backend.team.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import ok.backend.chat.domain.entity.ChatRoom;
import ok.backend.schedule.domain.entity.TeamSchedule;
import ok.backend.team.dto.TeamUpdateRequestDto;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Table(name = "teams")
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatroom;

    @Column(nullable = false, length = 10)
    private String name;

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    @CreationTimestamp
    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamList> teamList = new ArrayList<>();

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamSchedule> teamSchedules = new ArrayList<>();


    public void updateName(TeamUpdateRequestDto teamUpdateRequestDto) {
        this.name = teamUpdateRequestDto.getName();
    }


}