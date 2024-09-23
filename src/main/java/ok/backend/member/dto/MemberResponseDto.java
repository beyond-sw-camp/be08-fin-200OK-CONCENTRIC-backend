package ok.backend.member.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ok.backend.member.domain.entity.Member;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
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

    public MemberResponseDto(Member member) {
        this.id = member.getId();
        this.name = member.getName();
        this.nickname = member.getNickname();
        this.createDate = member.getCreateDate();
        this.imageUrl = member.getImageUrl();
        this.content = member.getContent();
    }
}
