package ok.backend.member.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class MemberUpdateRequestDto {

    @NotNull
    private String nickname;

    private String imageUrl;

    private String content;
}
