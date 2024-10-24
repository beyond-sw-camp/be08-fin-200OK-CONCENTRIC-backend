package ok.backend.team.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
@Data
@AllArgsConstructor
public class TeamUpdateRequestDto {

    private String name;

}
