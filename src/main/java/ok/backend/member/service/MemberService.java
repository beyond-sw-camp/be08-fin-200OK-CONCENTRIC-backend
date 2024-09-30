package ok.backend.member.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ok.backend.common.exception.CustomException;
import ok.backend.common.exception.ErrorCode;
import ok.backend.common.security.util.JwtProvider;
import ok.backend.common.security.util.SecurityUserDetailService;
import ok.backend.member.domain.entity.Member;
import ok.backend.member.domain.entity.RefreshToken;
import ok.backend.member.domain.enums.MemberStatus;
import ok.backend.member.domain.repository.MemberRepository;
import ok.backend.member.dto.MemberLoginRequestDto;
import ok.backend.member.dto.MemberRegisterRequestDto;
import ok.backend.member.dto.MemberUpdateRequestDto;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final PasswordEncoder passwordEncoder;

    private final MemberRepository memberRepository;

    private final JwtProvider jwtProvider;

    private final RefreshTokenService refreshTokenService;

    private final SecurityUserDetailService securityUserDetailService;

    @Transactional
    public Optional<Member> registerMember(MemberRegisterRequestDto memberRegisterRequestDto) {
        Optional<Member> exist = memberRepository.findByEmail(memberRegisterRequestDto.getEmail());
        if (exist.isPresent()) {
            throw  new CustomException(ErrorCode.DUPLICATE_SIGNUP_ID);
        }

        String hashPassword = passwordEncoder.encode(memberRegisterRequestDto.getPassword());

        Member member = Member.builder()
                .email(memberRegisterRequestDto.getEmail())
                .password(hashPassword)
                .name(memberRegisterRequestDto.getName())
                .nickname(memberRegisterRequestDto.getNickname())
                .status(MemberStatus.Y)
                .build();

        return Optional.of(memberRepository.save(member));
    }

    public Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email).orElseThrow(() ->
                new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    public Member findMemberByEmailAndPassword(MemberLoginRequestDto memberLoginRequestDto) {
        Member member = memberRepository.findByEmail(memberLoginRequestDto.getEmail()).orElseThrow(() ->
                new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if(!passwordEncoder.matches(memberLoginRequestDto.getPassword(), member.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }
        if(member.getStatus() == MemberStatus.N) {
            throw new CustomException(ErrorCode.MEMBER_DELETED);
        }

        return member;
    }

    @Transactional
    public ResponseCookie createToken(Member member) {

        String accessToken = jwtProvider.createAccessToken(member.getEmail());
        String refreshToken = jwtProvider.createRefreshToken(member.getEmail());

        RefreshToken newRefreshToken = RefreshToken.builder()
                .username(member.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiration(new Date(new Date().getTime() + jwtProvider.getRefreshTokenValidTime()).getTime())
                .build();

        refreshTokenService.save(newRefreshToken);

        ResponseCookie cookie = ResponseCookie.from(jwtProvider.getAccessHeader(), accessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(3600)
                .build();

        log.info(accessToken);
        log.info(refreshToken);
        log.info("token created");

        return cookie;
    }

    @Transactional
    public void logout(HttpServletRequest request){
        Cookie accessTokenCookie = jwtProvider.resolveAccessToken(request).orElseThrow(() ->
                new CustomException(ErrorCode.TOKEN_NOT_EXIST));
        String accessToken = accessTokenCookie.getValue();

        if(!jwtProvider.getUserPK(accessToken).equals(securityUserDetailService.getLoggedInMember().getEmail())){
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        RefreshToken refreshToken = refreshTokenService.findByAccessToken(accessToken).orElseThrow(() ->
                new CustomException(ErrorCode.REFRESH_TOKEN_NOT_EXIST));

        refreshTokenService.delete(refreshToken);

        SecurityContextHolder.clearContext();
    }

    @Transactional
    public Member updateMember(MemberUpdateRequestDto memberUpdateRequestDto){
        Member loggedInMember = securityUserDetailService.getLoggedInMember();
        Member member = memberRepository.findById(loggedInMember.getId()).orElseThrow(() ->
                new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        member.updateMember(memberUpdateRequestDto);

        return memberRepository.save(member);
    }

    @Transactional
    public void deleteMember(HttpServletRequest request){
        Member loggedInMember = securityUserDetailService.getLoggedInMember();
        Member member = memberRepository.findById(loggedInMember.getId()).orElseThrow(() ->
                new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        member.updateStatus();
        memberRepository.save(member);

        logout(request);
    }

    @Transactional
    public void updatePassword(String email, String code){
        Member member = memberRepository.findByEmail(email).orElseThrow(() ->
                new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        String newPassword = passwordEncoder.encode(code);

        member.updatePassword(newPassword);
        memberRepository.save(member);
    }
}
