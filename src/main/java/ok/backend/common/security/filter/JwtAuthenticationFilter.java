package ok.backend.common.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import ok.backend.common.security.util.JwtTokenProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Optional<Cookie> cookie = jwtTokenProvider.resolveAccessToken(request);
        if(cookie.isEmpty()){
            System.out.println("cookie is empty");
        }
        if(cookie.isPresent()){
            String token = cookie.get().getValue();
            boolean isValid = jwtTokenProvider.validateToken(token);
            Authentication authentication;

            if(isValid){
                authentication = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }else{
                response = jwtTokenProvider.reIssueAccessToken(request);
            }
        }
    }
}
