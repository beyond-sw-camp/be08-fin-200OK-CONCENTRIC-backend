package ok.backend.member.service;

import lombok.RequiredArgsConstructor;
import ok.backend.member.domain.entity.RefreshToken;
import ok.backend.member.domain.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    public Optional<RefreshToken> findByAccessToken(String accessToken) {
        return refreshTokenRepository.findByAccessToken(accessToken);
    }

    public void updateAccessToken(RefreshToken refreshToken, String newAccessToken) {
        refreshToken.updateAccessToken(newAccessToken);
        refreshTokenRepository.save(refreshToken);
    }

    public void delete(RefreshToken refreshToken) {
        refreshTokenRepository.delete(refreshToken);
    }

    public void save(RefreshToken refreshToken) {
        refreshTokenRepository.save(refreshToken);
    }
}
