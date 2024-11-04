package ok.backend.member.domain.entity;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;

@Builder
@Getter
@RedisHash(value = "Invite", timeToLive = 604800)
public class Invite {

    @Id
    private String id;

    private String email;

    private Long teamId;
}
