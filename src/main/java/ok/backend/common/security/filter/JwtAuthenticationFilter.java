package ok.backend.common.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import ok.backend.common.exception.CustomException;
import ok.backend.common.exception.ErrorCode;
import ok.backend.common.security.util.JwtProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        Optional<Cookie> accessTokenCookie = jwtProvider.resolveAccessToken(request);
        if(accessTokenCookie.isEmpty()){
            System.out.println("accessTokenCookie is empty");
        }
        if(accessTokenCookie.isPresent()){
            String accessToken = accessTokenCookie.get().getValue();
            boolean isValid = jwtProvider.validateToken(accessToken);

            if(isValid){
                Authentication authentication = jwtProvider.getAuthentication(accessToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }else{
                Cookie cookie = jwtProvider.reIssueAccessToken(accessToken);

                if(cookie != null){
                    response.addCookie(cookie);
                }else{
                    throw new CustomException(ErrorCode.UNAUTHORIZED);
                }
            }
        }
        chain.doFilter(request, response);
    }
}
