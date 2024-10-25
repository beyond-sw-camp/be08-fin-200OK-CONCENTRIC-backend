package ok.backend.member.service;

import ok.backend.common.exception.CustomException;
import ok.backend.common.exception.ErrorCode;
import ok.backend.common.security.util.SecurityUser;
import ok.backend.member.MemberBase;
import ok.backend.member.MockMember;
import ok.backend.member.domain.entity.Member;
import ok.backend.member.dto.MemberRegisterRequestDto;
import ok.backend.member.dto.MemberUpdateRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class MemberServiceTest extends MemberBase {

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
        Member found = memberRepository.findByNickname(requestDto.getNickname()).get();
        System.out.println(requestDto.getNickname());

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
        when(storageFileService.saveProfileImage(member.getId(), member.getImageUrl(), multipartFile)).thenReturn("profile");
        when(storageFileService.saveBackgroundImage(member.getId(), member.getBackground(), multipartFile)).thenReturn("background");

        // when

    }
}
