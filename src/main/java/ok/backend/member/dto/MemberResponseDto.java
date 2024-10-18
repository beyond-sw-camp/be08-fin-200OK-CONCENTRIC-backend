package ok.backend.member.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import ok.backend.member.domain.entity.Member;

import java.time.LocalDate;

@Getter
public class MemberResponseDto {

    @JsonProperty
    private Long id;

    @JsonProperty
    private String name;

    @JsonProperty
    private String nickname;

    @JsonProperty
    private LocalDate createDate;

    @JsonProperty
    private String imageUrl;

    @JsonProperty
    private String content;

    @JsonProperty
    private String background;


    public MemberResponseDto(Member member) {
        this.id = member.getId();
        this.name = member.getName();
        this.nickname = member.getNickname();
        this.createDate = member.getCreateDate();
        this.imageUrl = member.getImageUrl();
        this.content = member.getContent();
        this.background = member.getBackground();
    }
}
