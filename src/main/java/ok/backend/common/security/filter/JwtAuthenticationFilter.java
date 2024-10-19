package ok.backend.common.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import ok.backend.common.security.util.JwtProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        try {
            String accessToken = jwtProvider.resolveAccessToken(request);
            if(accessToken == null){
                System.out.println("accessToken Not Found");
            }
            System.out.println("request " + accessToken);
            if(accessToken != null){
                boolean isValid = jwtProvider.validateToken(accessToken);

                if(isValid){
                    Authentication authentication = jwtProvider.getAuthentication(accessToken);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }else{
                    System.out.println("accessToken Expired");
                    String newAccessToken = jwtProvider.reIssueAccessToken(accessToken);

                    if(newAccessToken != null) {
                        response.setHeader("Authorization", "Bearer " + newAccessToken);
                        System.out.println("accessToken ReIssued");
                    }
//                    }else{
//                        throw new CustomException(ErrorCode.UNAUTHORIZED);
//                    }
                }
            }
            chain.doFilter(request, response);
        } catch (Exception e) {
            System.out.println("catched Exception");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}
