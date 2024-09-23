package ok.backend.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import ok.backend.member.domain.entity.Member;
import ok.backend.member.dto.MemberLoginRequestDto;
import ok.backend.member.dto.MemberRegisterRequestDto;
import ok.backend.member.dto.MemberResponseDto;
import ok.backend.member.service.MemberService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "회원 관리")
@RequiredArgsConstructor
@RestController
@RequestMapping("v1/api/member")
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "회원 정보를 등록하는 API")
    @PostMapping("/register")
    public ResponseEntity<String> registerMember(@RequestBody MemberRegisterRequestDto memberRegisterRequestDto) {
        memberService.registerMember(memberRegisterRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "로그인 API")
    @PostMapping("/login")
    public ResponseEntity<MemberResponseDto> login(@RequestBody MemberLoginRequestDto memberLoginRequestDto) {
        Member member = memberService.findMemberByEmailAndPassword(memberLoginRequestDto);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, memberService.createToken(member).toString())
                .body(new MemberResponseDto(member));
    }
}
