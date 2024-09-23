package ok.backend.member.domain.entity;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

@Builder
@Getter
@RedisHash(value = "RefreshToken", timeToLive = 86400)
public class RefreshToken {

    @Id
    private Long id;

    @Indexed
    private String accessToken;

    private String refreshToken;

    private String username;

    @TimeToLive
    private Long expiration;

    public void updateAccessToken(String newAccessToken) {
        this.accessToken = newAccessToken;
    }
}
