package ok.backend.member.domain.entity;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@Builder
@Getter
@RedisHash(value = "Email", timeToLive = 300)
public class Email {

    @Id
    private Long id;

    @Indexed
    private String email;

    private String verificationCode;
}
