package ok.backend.member.service;

import lombok.RequiredArgsConstructor;
import ok.backend.member.domain.entity.RefreshToken;
import ok.backend.member.domain.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshToken findByAccessToken(String accessToken) {
        return refreshTokenRepository.findByAccessToken(accessToken).orElse(null);
    }

    public void updateAccessToken(RefreshToken refreshToken, String newAccessToken) {
        refreshToken.updateAccessToken(newAccessToken);
        refreshTokenRepository.save(refreshToken);
    }
}
