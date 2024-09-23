package ok.backend.common.security.util;

import lombok.RequiredArgsConstructor;
import ok.backend.member.domain.entity.Member;
import ok.backend.member.domain.repository.MemberRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecurityUserDetailService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(username).orElseThrow(() ->
                new UsernameNotFoundException(username + " not found"));
        return new SecurityUser(member);
//        Optional<Member> optional = memberRepository.findByEmail(username);
//        if(optional.isEmpty()) {
//            throw new UsernameNotFoundException(username + " 사용자 없음");
//        } else {
//            Member member = optional.get();
//            return new SecurityUser(member);
//        }
    }
}