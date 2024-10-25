package ok.backend.member;

import ok.backend.common.security.util.JwtProvider;
import ok.backend.common.security.util.SecurityUserDetailService;
import ok.backend.member.domain.entity.Email;
import ok.backend.member.domain.entity.Member;
import ok.backend.member.domain.entity.RefreshToken;
import ok.backend.member.domain.repository.MemberRepository;
import ok.backend.member.dto.MemberRegisterRequestDto;
import ok.backend.member.dto.MemberUpdateRequestDto;
import ok.backend.member.service.MemberService;
import ok.backend.member.service.RefreshTokenService;
import ok.backend.storage.service.AwsFileService;
import ok.backend.storage.service.StorageFileService;
import ok.backend.storage.service.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import static org.mockito.Mockito.mock;

public abstract class MemberBase {

    @Spy
    protected PasswordEncoder passwordEncoder;

    @Mock
    protected MemberRepository memberRepository;

    @Mock
    protected JwtProvider jwtProvider;

    @Mock
    protected RefreshTokenService refreshTokenService;

    @Mock
    protected SecurityUserDetailService securityUserDetailService;

    @Mock
    protected StorageService storageService;

    @Mock
    protected StorageFileService storageFileService;

    @Mock
    protected AwsFileService awsFileService;

    @InjectMocks
    protected MemberService memberService;

    protected Member member;
    protected RefreshToken refreshToken;
    protected Email email;
    protected Member existMember;
    protected MemberRegisterRequestDto memberRegisterRequestDto;
    protected MemberUpdateRequestDto memberUpdateRequestDto;
    protected MultipartFile multipartFile;
    protected Member loggedInMember;

    @BeforeEach
    public void setUp(){
        MockitoAnnotations.openMocks(this);

        member = mock(Member.class);
        existMember = mock(Member.class);
//        loggedInMember = mock(Member.class);

        refreshToken = mock(RefreshToken.class);
        email = mock(Email.class);
        passwordEncoder = new BCryptPasswordEncoder();

        multipartFile = mock(MultipartFile.class);

        memberRegisterRequestDto = mock(MemberRegisterRequestDto.class);
        memberUpdateRequestDto = mock(MemberUpdateRequestDto.class);

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
                .nickname("nickname")
                .password(passwordEncoder.encode("password2"))
                .isActive(true)
                .content("content2")
                .imageUrl("imageUrl2")
                .background("background2")
                .build();

        memberRegisterRequestDto = new MemberRegisterRequestDto("test@test.com", "password", "test", "nickname");

        memberUpdateRequestDto = new MemberUpdateRequestDto("nickname", "content");

//        when(securityUserDetailService.getLoggedInMember()).thenReturn(loggedInMember);
    }

}
