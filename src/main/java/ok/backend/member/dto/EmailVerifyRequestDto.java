package ok.backend.member.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EmailVerifyRequestDto {

    @NotNull
    private String email;

    @NotNull
    private String verificationCode;
}
