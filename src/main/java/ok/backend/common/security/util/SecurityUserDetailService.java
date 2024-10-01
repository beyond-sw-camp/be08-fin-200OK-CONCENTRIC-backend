package ok.backend.common.security.util;

import lombok.RequiredArgsConstructor;
import ok.backend.common.exception.CustomException;
import ok.backend.common.exception.ErrorCode;
import ok.backend.member.domain.entity.Member;
import ok.backend.member.domain.repository.MemberRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecurityUserDetailService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws CustomException {
        Member member = memberRepository.findByEmail(username).orElseThrow(() ->
                new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        if(!member.getIsActive()){
            throw new CustomException(ErrorCode.MEMBER_DELETED);
        }
        return new SecurityUser(member);
    }

    public Member getLoggedInMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        return securityUser.getMember();
    }
}