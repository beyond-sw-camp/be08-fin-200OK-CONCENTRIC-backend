package ok.backend.common.security.util;

import lombok.Getter;
import ok.backend.member.domain.entity.Member;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;

@Getter
public class SecurityUser extends User {
    private Member member;

    public SecurityUser(Member member) {
        super(member.getId().toString(), member.getPassword(),
                AuthorityUtils.createAuthorityList("USER"));
        this.member = member;
    }

}
