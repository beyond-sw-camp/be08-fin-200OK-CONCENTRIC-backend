package ok.backend.common.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access.header}")
    private String accessHeader;

    @Value("${jwt.access.expiration}")
    private long accessTokenValidTime;

    @Value("${jwt.refresh.header}")
    private String refreshHeader;

    @Value("${jwt.refresh.expiration}")
    private long refreshTokenValidTime;

    private final SecurityUserDetailService securityUserDetailService;

    private final SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());

    public String createToken(String sub, String userPK, long tokenValidTime) {

        Header header = Jwts.header()
                .add("typ", "JWT")
                .build();

        Claims claims = Jwts.claims()
                .add("sub", sub)
                .add("user_id", userPK)
                .build();

//        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());

        Date now = new Date();

        return Jwts.builder()
                .header().add(header).and()
                .claims(claims)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + tokenValidTime * 1000L))
                .issuer("200ok")
                .signWith(key)
                .compact();
    }

    public String createAccessToken(String sub, String userPk){
        return createToken(sub, userPk, accessTokenValidTime);
    }

    public String createRefreshToken(String sub, String userPk){
        return createToken(sub, userPk, refreshTokenValidTime);
    }

    public Authentication getAuthentication(String token) {
        UserDetails userDetails = securityUserDetailService.loadUserByUsername(this.getUserPK(token));

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    public String getUserPK(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().get("user_id").toString();
    }

    public String getUserRole(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().get("role").toString();
    }

    public Optional<Cookie> resolveAccessToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(accessHeader)) {
                    return Optional.of(cookie);
                }
            }
        }

        return Optional.empty();
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return !claims.getPayload().getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public HttpServletResponse reIssueAccessToken(HttpServletRequest request){

    }
}
