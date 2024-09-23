package ok.backend.member.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.util.ArrayList;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Table(name = "Users")
public class Member {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 30)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 10)
    private String name;

    @Column(nullable = false, length = 20)
    private String nickname;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDate createDate;

    @Column(nullable = false)
    private MemberStatus status;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "profile_content")
    private String content;

    @OneToMany(mappedBy = "users")
    private List<Notification> notifications = new ArrayList<>();

    @OneToMany(mappedBy = "users")
    private List<Schedule> schedules = new ArrayList<>();

    @OneToMany(mappedBy = "users")
//    @OnDelete(action = OnDeleteAction.CASCADE)
//      -> CascadeType.REMOVE 를 여기서 적용 or 위 annotation 을 N쪽에 적용
    private List<TeamList> teamList = new ArrayList<>();

    @OneToMany(mappedBy = "users")
    private List<ChatRoomList> chatRoomList = new ArrayList<>();

    @OneToMany(mappedBy = "users")
    private List<ChatMessage> chatMessages = new ArrayList<>();

//    @OneToMany(mappedBy = "users")
//    private List<Comment> comments = new ArrayList<>();
}
