package ok.backend.member.service;

import ok.backend.common.exception.CustomException;
import ok.backend.common.exception.ErrorCode;
import ok.backend.common.security.util.JwtProvider;
import ok.backend.common.security.util.SecurityUser;
import ok.backend.common.security.util.SecurityUserDetailService;
import ok.backend.member.MockMember;
import ok.backend.member.domain.entity.Email;
import ok.backend.member.domain.entity.Member;
import ok.backend.member.domain.entity.RefreshToken;
import ok.backend.member.domain.repository.MemberRepository;
import ok.backend.member.dto.MemberLoginRequestDto;
import ok.backend.member.dto.MemberRegisterRequestDto;
import ok.backend.member.dto.MemberUpdateRequestDto;
import ok.backend.storage.service.AwsFileService;
import ok.backend.storage.service.StorageFileService;
import ok.backend.storage.service.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
//@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    @Spy
    private PasswordEncoder passwordEncoder;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private SecurityUserDetailService securityUserDetailService;

    @Mock
    private StorageService storageService;

    @Mock
    private StorageFileService storageFileService;

    @Mock
    private AwsFileService awsFileService;

    @InjectMocks
    private MemberService memberService;

    private Member member;
    private RefreshToken refreshToken;
    private Email email;
    private Member existMember;
    private Member deletedMember;
    private MemberRegisterRequestDto memberRegisterRequestDto;
    private MemberUpdateRequestDto memberUpdateRequestDto;
    private MemberLoginRequestDto memberLoginRequestDto;
    private MultipartFile multipartFile;

    @BeforeEach
    public void setUp() {

        member = mock(Member.class);
        existMember = mock(Member.class);
        deletedMember = mock(Member.class);

        refreshToken = mock(RefreshToken.class);
        email = mock(Email.class);
        passwordEncoder = new BCryptPasswordEncoder();

        multipartFile = mock(MultipartFile.class);

        memberRegisterRequestDto = mock(MemberRegisterRequestDto.class);
        memberUpdateRequestDto = mock(MemberUpdateRequestDto.class);
        memberLoginRequestDto = mock(MemberLoginRequestDto.class);


        member = Member.builder()
                .id(1L)
                .email("test@test.com")
                .name("test")
                .nickname("nickname")
                .password(passwordEncoder.encode("password"))
                .isActive(true)
                .content("content")
                .imageUrl("imageUrl")
                .background("background")
                .build();

        existMember = Member.builder()
                .id(2L)
                .email("duplicate@test.com")
                .name("test2")
                .nickname("nickname2")
                .password(passwordEncoder.encode("password2"))
                .isActive(true)
                .content("content2")
                .imageUrl("imageUrl2")
                .background("background2")
                .build();

        deletedMember = Member.builder()
                .id(3L)
                .email("test@test.com")
                .isActive(false)
                .password(passwordEncoder.encode("password"))
                .build();

        refreshToken = RefreshToken.builder()
                .id(1L)
                .username("test@test.com")
                .accessToken("access_token")
                .refreshToken("refresh_token")
                .build();

        memberRegisterRequestDto = new MemberRegisterRequestDto("test@test.com", "password", "test", "nickname");

        memberUpdateRequestDto = new MemberUpdateRequestDto("nickname2", "content");

        memberLoginRequestDto = new MemberLoginRequestDto("test@test.com", "password");

        when(securityUserDetailService.getLoggedInMember()).thenReturn(member);
    }

    @Test
    @MockMember()
    void check(){

        SecurityUser securityUser = (SecurityUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println(securityUser.getMember().getId());
    }

    @Test
    @DisplayName("회원 가입 - 성공")
    void registerMember_success(){
        // given
        MemberRegisterRequestDto requestDto = memberRegisterRequestDto;
        Member member1 = Member.builder()
                .id(1L)
                .email(requestDto.getEmail())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .nickname(requestDto.getNickname())
                .name(requestDto.getName())
                .isActive(true)
                .build();

        // when
        when(memberRepository.save(member)).thenReturn(member1);

        // then
        Member savedMember = memberRepository.save(member);
        assertNotNull(savedMember);
        assertEquals(requestDto.getEmail(), savedMember.getEmail());
        assertEquals(requestDto.getName(), savedMember.getName());
        assertEquals(requestDto.getNickname(), savedMember.getNickname());
        assertTrue(passwordEncoder.matches(requestDto.getPassword(), savedMember.getPassword()));
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    @DisplayName("회원 가입 - 실패 (닉네임 중복)")
    void registerMember_fail_duplicate_nickname(){
        // given
        MemberRegisterRequestDto requestDto = memberRegisterRequestDto;
        when(memberRepository.findByNickname(requestDto.getNickname())).thenReturn(Optional.of(existMember));

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
           memberService.checkNickNameExist(requestDto.getNickname());
        });

        // then
        assertEquals(ErrorCode.DUPLICATE_NICKNAME, exception.getErrorCode());
    }

    @Test
    @DisplayName("회원 정보 수정 - 성공")
    @MockMember
    void updateMember_success() throws IOException {
        // given
        SecurityUser securityUser = (SecurityUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member loggedInMember = securityUser.getMember();

        MemberUpdateRequestDto requestDto = memberUpdateRequestDto;

        when(memberRepository.findById(loggedInMember.getId())).thenReturn(Optional.of(member));
        when(storageFileService.saveProfileImage(member.getId(), member.getImageUrl(), multipartFile)).thenReturn("imageUrl");
        when(storageFileService.saveBackgroundImage(member.getId(), member.getBackground(), multipartFile)).thenReturn("background");

        // when
        Member found = memberRepository.findById(loggedInMember.getId()).orElseThrow(() ->
                new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        found.updateMember(requestDto);
        String profilePath = storageFileService.saveProfileImage(member.getId(), member.getImageUrl(), multipartFile);
        String backgroundPath = storageFileService.saveBackgroundImage(member.getId(), member.getBackground(), multipartFile);

        // then
        assertNotNull(loggedInMember);
        assertNotNull(found);
        assertEquals(loggedInMember.getId(), found.getId());
        assertEquals(member.getNickname(), found.getNickname());
        assertEquals(member.getContent(), found.getContent());
        assertEquals(member.getImageUrl(), profilePath);
        assertEquals(member.getBackground(), backgroundPath);
    }

    @Test
    @DisplayName("회원 정보 수정 - 실패 (닉네임 중복)")
    @MockMember
    void updateMember_fail_duplicate_nickname(){
        // given
        MemberUpdateRequestDto requestDto = memberUpdateRequestDto;
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberRepository.findByNickname(requestDto.getNickname())).thenReturn(Optional.of(existMember));

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
           memberService.updateMember(requestDto, multipartFile, multipartFile);
        });

        // then
        assertEquals(ErrorCode.DUPLICATE_NICKNAME, exception.getErrorCode());
    }

    @Test
    @DisplayName("회원 탈퇴 - 성공")
    @MockMember
    void deleteMember_success() {
        // given
        Member member1 = member;

        MemberLoginRequestDto requestDto = memberLoginRequestDto;

        when(memberRepository.findByEmail(requestDto.getPassword())).thenReturn(Optional.of(member1));
        when(refreshTokenService.findByUsername(requestDto.getEmail())).thenReturn(Optional.empty());

        // when
        member1.updateStatus();
        memberRepository.save(member1);

        storageService.deletePrivateStorage(member1.getId());

        refreshTokenService.delete(refreshToken);

        CustomException exception = assertThrows(CustomException.class, () -> {
            refreshTokenService.findByUsername(requestDto.getEmail()).orElseThrow(() -> new CustomException(ErrorCode.REFRESH_TOKEN_NOT_EXIST));
        });

        // then
        assertEquals(requestDto.getEmail(), member1.getEmail());
        assertTrue(passwordEncoder.matches(requestDto.getPassword(), member1.getPassword()));
        assertEquals(ErrorCode.REFRESH_TOKEN_NOT_EXIST, exception.getErrorCode());
    }

    @Test
    @DisplayName("회원 탈퇴 - 실패 (토큰 없음)")
    @MockMember
    void deleteMember_fail_token_not_exist() {
        // given
        Member member1 = member;

        MemberLoginRequestDto requestDto = memberLoginRequestDto;

        when(memberRepository.findByEmail(requestDto.getPassword())).thenReturn(Optional.of(member1));
        when(refreshTokenService.findByUsername(requestDto.getEmail())).thenReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            refreshTokenService.findByUsername(requestDto.getEmail()).orElseThrow(() -> new CustomException(ErrorCode.REFRESH_TOKEN_NOT_EXIST));
        });

        // then
        assertEquals(ErrorCode.REFRESH_TOKEN_NOT_EXIST, exception.getErrorCode());
    }

    @Test
    @DisplayName("회원 탈퇴 - 실패 (잘못된 비밀번호)")
    @MockMember
    void deleteMember_fail_invalid_password() {
        // given
        Member member1 = existMember;

        MemberLoginRequestDto requestDto = memberLoginRequestDto;

        when(memberRepository.findByEmail(requestDto.getEmail())).thenReturn(Optional.of(member1));

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            Member found = memberRepository.findByEmail(requestDto.getEmail()).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
            if(!passwordEncoder.matches(found.getPassword(), member.getPassword())) {
                throw new CustomException(ErrorCode.INVALID_PASSWORD);
            }
        });

        // then
        assertEquals(ErrorCode.INVALID_PASSWORD, exception.getErrorCode());
    }

    @Test
    @DisplayName("비밀번호 변경 - 성공")
    @MockMember
    void update_password_success() {
        // given
        SecurityUser securityUser = (SecurityUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Member loggedInMember = securityUser.getMember();

        String current = "current";

        when(memberRepository.findByEmail(loggedInMember.getEmail())).thenReturn(Optional.of(member));

        // when
        Member member1 = memberRepository.findByEmail(loggedInMember.getEmail()).orElseThrow(() ->
                new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        String currentPassword = passwordEncoder.encode(current);
        member1.updatePassword(currentPassword);

        // then
        assertTrue(passwordEncoder.matches(current, member1.getPassword()));
    }

    @Test
    @DisplayName("비밀번호 변경 - 실패 (잘못된 비밀번호)")
    @MockMember
    void update_password_fail_invalid_password() {
        // given
        when(memberRepository.findById(1L)).thenReturn(Optional.of(existMember));
        String current = "current";
        String previous = "previous";

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            memberService.updatePasswordByPrevious(current, previous);
        });

        // then
        assertEquals(ErrorCode.INVALID_PASSWORD, exception.getErrorCode());
    }

    @Test
    @DisplayName("로그인 - 성공")
    void login_success() {
        // given
        MemberLoginRequestDto requestDto = memberLoginRequestDto;

        when(memberRepository.findByEmail(requestDto.getEmail())).thenReturn(Optional.of(member));

        // when
        Member found = memberRepository.findByEmail(requestDto.getEmail()).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // then
        assertEquals(requestDto.getEmail(), found.getEmail());
        assertTrue(passwordEncoder.matches(requestDto.getPassword(), found.getPassword()));
    }

    @Test
    @DisplayName("로그인- - 실패 (계정 없음)")
    void login_fail_email_not_exist() {
        // given
        MemberLoginRequestDto requestDto = memberLoginRequestDto;

        when(memberRepository.findByEmail(requestDto.getEmail())).thenReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
           memberService.findMemberByEmailAndPassword(memberLoginRequestDto);
        });

        // then
        assertEquals(ErrorCode.MEMBER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("로그인 - 실패 (잘못된 비밀번호)")
    void login_fail_password_invalid() {
        // given
        MemberLoginRequestDto requestDto = memberLoginRequestDto;

        when(memberRepository.findByEmail(requestDto.getEmail())).thenReturn(Optional.of(existMember));

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
           memberService.findMemberByEmailAndPassword(memberLoginRequestDto);
        });

        // then
        assertEquals(ErrorCode.INVALID_PASSWORD, exception.getErrorCode());
    }

    @Test
    @DisplayName("로그인 - 실패 (탈퇴한 계정)")
    void login_fail_deactivated() {
        // given
        MemberLoginRequestDto requestDto = memberLoginRequestDto;

        when(memberRepository.findByEmail(requestDto.getEmail())).thenReturn(Optional.of(deletedMember));

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
           memberService.findMemberByEmailAndPassword(requestDto);
        });

        // then
        assertEquals(ErrorCode.MEMBER_DELETED, exception.getErrorCode());
    }

    @Test
    @DisplayName("로그아웃 - 성공")
    @MockMember
    void logout_success() {
        // given
        when(refreshTokenService.findByUsername(member.getEmail())).thenReturn(Optional.of(refreshToken));

        // when
        assertDoesNotThrow(() -> memberService.logout());

    }

    @Test
    @DisplayName("로그아웃 - 실패")
    @MockMember
    void logout_fail() {
        // given
        when(refreshTokenService.findByUsername(member.getEmail())).thenReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            memberService.logout();
        });

        // then
        assertEquals(ErrorCode.REFRESH_TOKEN_NOT_EXIST, exception.getErrorCode());
    }

    @Test
    @DisplayName("액세스 토큰 발급")
    void issue_access_token() {
        // given
        when(jwtProvider.createAccessToken(member.getEmail())).thenReturn("access_token");
        when(jwtProvider.createRefreshToken(member.getEmail())).thenReturn("refresh_token");

        // when
        String token = memberService.createToken(member);

        // then
        assertEquals(token, "access_token");
    }

    @Test
    @DisplayName("리프레시 토큰 발급")
    void issue_refresh_token() {
        // given
        when(jwtProvider.createAccessToken(member.getEmail())).thenReturn("access_token");
        when(jwtProvider.createRefreshToken(member.getEmail())).thenReturn("refresh_token");

        // when
        String token = memberService.createToken(member);

        // then
        assertEquals(token, "access_token");
        assertEquals(refreshToken.getAccessToken(), token);
        assertEquals(refreshToken.getRefreshToken(), "refresh_token");
        assertEquals(refreshToken.getUsername(), member.getEmail());
    }

    @Test
    @DisplayName("액세스 토큰 만료시 재발급 - 성공")
    @MockMember
    void reissue_access_token_success() {
        // given
        String accessToken = "access_token";
        String newAccessToken = "new_access_token";

        when(jwtProvider.validateToken(accessToken)).thenReturn(false);
        when(jwtProvider.validateToken(newAccessToken)).thenReturn(true);
        when(jwtProvider.reIssueAccessToken(accessToken)).thenReturn(newAccessToken);

        // when
        String token = jwtProvider.reIssueAccessToken(accessToken);
        refreshToken.updateAccessToken(token);

        // then
        assertFalse(jwtProvider.validateToken(accessToken));
        assertTrue(jwtProvider.validateToken(newAccessToken));
        assertEquals(refreshToken.getAccessToken(), newAccessToken);
    }

    @Test
    @DisplayName("액세스 토큰 만료시 재발급 - 실패")
    @MockMember
    void reissue_access_token_fail() {
        // given
        String accessToken = "access_token";

        when(jwtProvider.validateToken(accessToken)).thenReturn(false);
        when(refreshTokenService.findByAccessToken(accessToken)).thenThrow(new CustomException(ErrorCode.REFRESH_TOKEN_NOT_EXIST));

        // when
        CustomException exception = assertThrows(CustomException.class, () ->
                refreshTokenService.findByAccessToken(accessToken));

        // then
        assertFalse(jwtProvider.validateToken(accessToken));
        assertEquals(ErrorCode.REFRESH_TOKEN_NOT_EXIST, exception.getErrorCode());
    }
}
