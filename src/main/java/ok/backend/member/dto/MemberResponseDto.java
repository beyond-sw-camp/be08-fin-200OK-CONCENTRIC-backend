package ok.backend.member.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Builder
@Getter
public class MemberResponseDto {

    @JsonProperty
    private Long id;

    @JsonProperty
    private String email;

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

}
