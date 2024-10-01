package ok.backend.friendship.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import ok.backend.friendship.domain.entity.FriendshipRequest;
import java.time.LocalDate;

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
    private String imageUrl;

    @JsonProperty
    private String content;

    public FriendshipRequestResponseDto(FriendshipRequest friendshipRequest) {
        this.id = friendshipRequest.getId();
        this.memberId = friendshipRequest.getMember().getId();
        this.nickname = friendshipRequest.getMember().getNickname();
        this.createDate = friendshipRequest.getMember().getCreateDate();
        this.imageUrl = friendshipRequest.getMember().getImageUrl();
        this.content = friendshipRequest.getMember().getContent();
    }

}
