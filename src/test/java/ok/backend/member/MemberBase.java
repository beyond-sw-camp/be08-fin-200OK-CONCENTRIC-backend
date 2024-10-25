package ok.backend.member;

import ok.backend.common.security.util.JwtProvider;
import ok.backend.common.security.util.SecurityUserDetailService;
import ok.backend.member.domain.entity.Email;
import ok.backend.member.domain.entity.Member;
import ok.backend.member.domain.entity.RefreshToken;
import ok.backend.member.domain.repository.MemberRepository;
import ok.backend.member.dto.*;
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
import static org.mockito.Mockito.when;

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

    @BeforeEach
    public void setUp(){
        MockitoAnnotations.openMocks(this);

        member = mock(Member.class);
        refreshToken = mock(RefreshToken.class);
        email = mock(Email.class);
        passwordEncoder = new BCryptPasswordEncoder();

        multipartFile = mock(MultipartFile.class);

        memberRegisterRequestDto = mock(MemberRegisterRequestDto.class);
        memberUpdateRequestDto = mock(MemberUpdateRequestDto.class);


        when(member.getId()).thenReturn(1L);
        when(member.getEmail()).thenReturn("test@test.com");
        when(member.getName()).thenReturn("test");
        when(member.getNickname()).thenReturn("nickname");
        when(member.getPassword()).thenReturn(passwordEncoder.encode("password"));
        when(member.getIsActive()).thenReturn(true);
        when(member.getContent()).thenReturn("content");
        when(member.getImageUrl()).thenReturn("imageUrl");
        when(member.getBackground()).thenReturn("background");
//        when(memberRepository.save(member)).thenReturn(member);

//        when(existMember.getId()).thenReturn(2L);
//        when(existMember.getEmail()).thenReturn("duplicate@test.com");
//        when(existMember.getName()).thenReturn("test2");
//        when(existMember.getNickname()).thenReturn("duplicate");
//        when(existMember.getPassword()).thenReturn(passwordEncoder.encode("password2"));
//        when(existMember.getIsActive()).thenReturn(true);
//        when(existMember.getContent()).thenReturn("content2");
//        when(existMember.getImageUrl()).thenReturn("imageUrl2");
//        when(existMember.getBackground()).thenReturn("background2");

        existMember = Member.builder()
                .id(2L)
                .email("duplicate@test.com")
                .name("test2")
                .nickname("duplicate")
                .password(passwordEncoder.encode("password2"))
                .isActive(true)
                .content("content2")
                .imageUrl("imageUrl2")
                .background("background2")
                .build();

        when(memberRegisterRequestDto.getEmail()).thenReturn("test@test.com");
        when(memberRegisterRequestDto.getPassword()).thenReturn("password");
        when(memberRegisterRequestDto.getNickname()).thenReturn("nickname");
        when(memberRegisterRequestDto.getName()).thenReturn("test");

        when(memberUpdateRequestDto.getNickname()).thenReturn("duplicate");
        when(memberUpdateRequestDto.getContent()).thenReturn("content");
    }

}
