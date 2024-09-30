package ok.backend.member.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ok.backend.chat.domain.entity.ChatMessage;
import ok.backend.chat.domain.entity.ChatRoomList;
import ok.backend.friendship.domain.entity.Friendship;
import ok.backend.friendship.domain.entity.FriendshipRequest;
import ok.backend.member.domain.enums.MemberStatus;
import ok.backend.member.dto.MemberUpdateRequestDto;
import ok.backend.schedule.domain.entity.Schedule;
import ok.backend.team.domain.entity.TeamList;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    @Column(unique = true, nullable = false, length = 20)
    private String nickname;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDate createDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "profile_content")
    private String content;

//    @OneToMany(mappedBy = "users")
//    private List<Notification> notifications = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Schedule> schedules = new ArrayList<>();

    @OneToMany(mappedBy = "member")
//    @OnDelete(action = OnDeleteAction.CASCADE)
//      -> CascadeType.REMOVE 를 여기서 적용 or 위 annotation 을 N쪽에 적용
    private List<TeamList> teamList = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<ChatRoomList> chatRoomList = new ArrayList<>();

//    @OneToMany(mappedBy = "member")
//    private List<ChatMessage> chatMessages = new ArrayList<>();

//    @OneToMany(mappedBy = "member")
//    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Friendship> friendships = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<FriendshipRequest> friendshipRequests = new ArrayList<>();

    public void updateMember(MemberUpdateRequestDto memberUpdateRequestDto) {
        this.nickname = memberUpdateRequestDto.getNickname();
        this.imageUrl = memberUpdateRequestDto.getImageUrl();
        this.content = memberUpdateRequestDto.getContent();
    }

    public void updateStatus(){
        this.status = MemberStatus.N;
    }

    public void updatePassword(String password){
        this.password = password;
    }
}
