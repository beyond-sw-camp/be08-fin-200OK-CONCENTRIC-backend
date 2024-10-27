package ok.backend.member;

import ok.backend.common.security.util.SecurityUser;
import ok.backend.member.domain.entity.Member;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithCustomSecurityContextFactory implements WithSecurityContextFactory<MockMember> {

    @Override
    public SecurityContext createSecurityContext(MockMember annotation) {
        Member mockMember = Member.builder()
                .id(annotation.id())
                .email(annotation.email())
                .password(annotation.password())
                .name(annotation.name())
                .nickname(annotation.nickname())
                .isActive(true)
                .build();

        UserDetails userDetails = new SecurityUser(mockMember);

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);

        return context;
    }
}
