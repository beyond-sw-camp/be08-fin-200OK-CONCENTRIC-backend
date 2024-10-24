package ok.backend.friendship.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import ok.backend.friendship.domain.entity.FriendshipRequest;
import java.time.LocalDate;

@Builder
@Getter
public class FriendshipRequestResponseDto {

    @JsonProperty
    private Long id;

    @JsonProperty
    private Long memberId;

    @JsonProperty
    private String nickname;

    @JsonProperty
    private LocalDate createDate;

    @JsonProperty
    private String profileImage;

    @JsonProperty
    private String content;

    @JsonProperty
    private String backgroundImage;

//    public FriendshipRequestResponseDto(FriendshipRequest friendshipRequest) {
//        this.id = friendshipRequest.getId();
//        this.memberId = friendshipRequest.getMember().getId();
//        this.nickname = friendshipRequest.getMember().getNickname();
//        this.createDate = friendshipRequest.getMember().getCreateDate();
//        this.imageUrl = friendshipRequest.getMember().getImageUrl();
//        this.content = friendshipRequest.getMember().getContent();
//        this.background = friendshipRequest.getMember().getBackground();
//    }

}
