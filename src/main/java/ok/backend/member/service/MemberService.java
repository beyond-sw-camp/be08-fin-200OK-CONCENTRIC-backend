package ok.backend.member.service;

import jakarta.transaction.Transactional;
import ok.backend.member.domain.entity.Member;
import ok.backend.member.domain.enums.MemberStatus;
import ok.backend.member.domain.repository.MemberRepository;
import ok.backend.member.dto.MemberRegisterRequestDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.xml.crypto.Data;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class MemberService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    public MemberService(PasswordEncoder passwordEncoder, MemberRepository memberRepository) {
        this.passwordEncoder = passwordEncoder;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public Optional<Member> registerMember(MemberRegisterRequestDto memberRegisterRequestDto) {

        String hashPassword = passwordEncoder.encode(memberRegisterRequestDto.getPassword());

        Member member = Member.builder()
                .email(memberRegisterRequestDto.getEmail())
                .password(hashPassword)
                .name(memberRegisterRequestDto.getName())
                .nickname(memberRegisterRequestDto.getNickname())
                .createDate(LocalDate.now())
                .status(MemberStatus.Y)
                .build();

        return Optional.of(memberRepository.save(member));
    }
}
