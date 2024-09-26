package ok.backend.friendship.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import ok.backend.member.domain.entity.Member;

public class FriendshipResponseDto {

    @JsonProperty
    private Long id;

    @JsonProperty
    private String nickname;

    @JsonProperty
    private String imageUrl;

    @JsonProperty
    private String content;

    public FriendshipResponseDto(Member member) {
        this.id = member.getId();
        this.nickname = member.getNickname();
        this.imageUrl = member.getImageUrl();
        this.content = member.getContent();
    }
}
