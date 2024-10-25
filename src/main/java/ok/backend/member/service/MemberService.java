package ok.backend.member.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ok.backend.common.exception.CustomException;
import ok.backend.common.exception.ErrorCode;
import ok.backend.common.security.util.JwtProvider;
import ok.backend.common.security.util.SecurityUserDetailService;
import ok.backend.member.domain.entity.Member;
import ok.backend.member.domain.entity.RefreshToken;
import ok.backend.member.domain.repository.MemberRepository;
import ok.backend.member.dto.*;
import ok.backend.storage.service.AwsFileService;
import ok.backend.storage.service.StorageFileService;
import ok.backend.storage.service.StorageService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final PasswordEncoder passwordEncoder;

    private final MemberRepository memberRepository;

    private final JwtProvider jwtProvider;

    private final RefreshTokenService refreshTokenService;

    private final SecurityUserDetailService securityUserDetailService;

    private final StorageService storageService;

    private final StorageFileService storageFileService;

    private final AwsFileService awsFileService;

    public MemberResponseDto registerMember(MemberRegisterRequestDto memberRegisterRequestDto) {
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
                .isActive(true)
                .build();

        Member savedMember = memberRepository.save(member);

        storageService.createPrivateStorage(savedMember.getId());

        return this.convertToDto(savedMember);
    }

    public Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email).orElseThrow(() ->
                new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    public void findMemberByToEmail(String toEmail) {
        Member member = memberRepository.findByEmail(toEmail).orElse(null);
        if(member != null){
            throw new CustomException(ErrorCode.DUPLICATE_SIGNUP_ID);
        }
    }

    public Member findMemberById(Long id) {
        Member member = memberRepository.findById(id).orElseThrow(() ->
                new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if(!member.getIsActive()){
            throw new CustomException(ErrorCode.MEMBER_DELETED);
        }

        return member;
    }

    public Member findMemberByEmailAndPassword(MemberLoginRequestDto memberLoginRequestDto) {
        Member member = memberRepository.findByEmail(memberLoginRequestDto.getEmail()).orElseThrow(() ->
                new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if(!passwordEncoder.matches(memberLoginRequestDto.getPassword(), member.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }
        if(!member.getIsActive()) {
            throw new CustomException(ErrorCode.MEMBER_DELETED);
        }

        return member;
    }

    public Member findMemberByNickname(String nickname) {
        return memberRepository.findByNicknameAndIsActiveTrue(nickname).orElseThrow(() ->
                new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    public MemberResponseDto convertToDto(Member member) {
        String backgroundImage = null;
        String profileImage = null;

        if(member.getBackground() != null){
            backgroundImage = awsFileService.getUrl(member.getBackground());
        }

        if(member.getImageUrl() != null){
            profileImage = awsFileService.getUrl(member.getImageUrl());
        }



        return MemberResponseDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .name(member.getName())
                .nickname(member.getNickname())
                .createDate(member.getCreateDate())
                .content(member.getContent())
                .imageUrl(profileImage)
                .background(backgroundImage)
                .build();
    }

    public String createToken(Member member) {

        Optional<RefreshToken> exist = refreshTokenService.findByUsername(member.getEmail());
        exist.ifPresent(refreshTokenService::delete);

        String accessToken = jwtProvider.createAccessToken(member.getEmail());
        String refreshToken = jwtProvider.createRefreshToken(member.getEmail());

        RefreshToken newRefreshToken = RefreshToken.builder()
                .username(member.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiration(new Date(new Date().getTime() + jwtProvider.getRefreshTokenValidTime()).getTime())
                .build();

        refreshTokenService.save(newRefreshToken);

        log.info(accessToken);
        log.info(refreshToken);
        log.info("token created");

        return accessToken;
    }

    public void logout(){
        Member loggedInMember = securityUserDetailService.getLoggedInMember();

        RefreshToken refreshToken = refreshTokenService.findByUsername(loggedInMember.getEmail()).orElseThrow(() ->
                new CustomException(ErrorCode.REFRESH_TOKEN_NOT_EXIST));

        refreshTokenService.delete(refreshToken);

        SecurityContextHolder.clearContext();
    }

    public MemberResponseDto updateMember(MemberUpdateRequestDto memberUpdateRequestDto, MultipartFile profile, MultipartFile background) throws IOException {
        Member loggedInMember = securityUserDetailService.getLoggedInMember();
        Member member = this.findMemberById(loggedInMember.getId());


        if(!member.getNickname().equals(memberUpdateRequestDto.getNickname())){
            Optional<Member> foundMember = memberRepository.findByNickname(memberUpdateRequestDto.getNickname());
            foundMember.ifPresent(member1 -> {
                throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
            });
        }

        member.updateMember(memberUpdateRequestDto);

        if(profile != null){
            String path = storageFileService.saveProfileImage(member.getId(), member.getImageUrl(), profile);
            member.updateProfileImage(path);
        }

        if(background != null){
            String path = storageFileService.saveBackgroundImage(member.getId(), member.getBackground(), background);
            member.updateBackgroundImage(path);
        }

        return this.convertToDto(memberRepository.save(member));
    }

    public void deleteMember(MemberLoginRequestDto memberLoginRequestDto){
        Member loggedInMember = securityUserDetailService.getLoggedInMember();
        Member member = this.findMemberByEmailAndPassword(memberLoginRequestDto);

        if(!member.getId().equals(loggedInMember.getId())){
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        member.updateStatus();
        memberRepository.save(member);

        SecurityContextHolder.clearContext();

        storageService.deletePrivateStorage(member.getId());

        RefreshToken refreshToken = refreshTokenService.findByUsername(member.getEmail()).orElseThrow(() ->
                new CustomException(ErrorCode.REFRESH_TOKEN_NOT_EXIST));

        refreshTokenService.delete(refreshToken);
    }

    public void updatePassword(String email, String code){
        Member member = memberRepository.findByEmail(email).orElseThrow(() ->
                new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        String newPassword = passwordEncoder.encode(code);

        member.updatePassword(newPassword);
        memberRepository.save(member);
    }

    public void updatePasswordByPrevious(String previous, String current){
        Member loggedInMember = securityUserDetailService.getLoggedInMember();
        Member member = this.findMemberById(loggedInMember.getId());

        if(!passwordEncoder.matches(previous, member.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        String currentPassword = passwordEncoder.encode(current);

        member.updatePassword(currentPassword);
    }

    public List<MemberProfileResponseDto> getMemberProfiles(List<Long> memberIdList) throws MalformedURLException {
        List<Member> memberList = memberRepository.findByIdAndIsActiveTrue(memberIdList);

        return this.getMemberProfilesByMemberList(memberList);
    }

    public List<MemberProfileResponseDto> getMemberProfilesByMemberList(List<Member> memberList) throws MalformedURLException {
        List<MemberProfileResponseDto> memberProfileResponseDtoList = new ArrayList<>();

        for(Member member : memberList){
            String backgroundImage = null;
            String profileImage = null;

            if(member.getBackground() != null){
                backgroundImage = awsFileService.getUrl(member.getBackground());
            }

            if(member.getImageUrl() != null){
                profileImage = awsFileService.getUrl(member.getImageUrl());
            }

            MemberProfileResponseDto dto = MemberProfileResponseDto.builder()
                    .id(member.getId())
                    .nickname(member.getNickname())
                    .createDate(member.getCreateDate())
                    .backgroundImage(backgroundImage)
                    .profileImage(profileImage)
                    .content(member.getContent())
                    .build();

            memberProfileResponseDtoList.add(dto);
        }

        return memberProfileResponseDtoList;
    }

    public void checkEmailExist(String email){
        Optional<Member> member = memberRepository.findByEmail(email);
        member.ifPresent(member1 -> {
            throw new CustomException(ErrorCode.DUPLICATE_SIGNUP_ID);
        });
    }

    public void checkNickNameExist(String nickname){
        Optional<Member> member = memberRepository.findByNickname(nickname);
        member.ifPresent(member1 -> {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        });
    }
}
