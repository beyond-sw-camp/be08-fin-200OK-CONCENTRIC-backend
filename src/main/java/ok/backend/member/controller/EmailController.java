package ok.backend.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ok.backend.member.dto.EmailVerifyRequestDto;
import ok.backend.member.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Email", description = "이메일 전송 관리")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/email")
public class EmailController {

    private final EmailService emailService;

    @Operation(summary = "이메일로 인증 코드를 발송하는 API")
    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(@RequestParam String toEmail) throws MessagingException {
        log.info("Sending email to " + toEmail);
        emailService.sendVerificationEmail(toEmail);

        return ResponseEntity.ok("인증 코드가 발송되었습니다.");
    }

    @Operation(summary = "이메일과 인증코드를 확인하는 API")
    @PostMapping("/verify")
    public ResponseEntity<String> verify(@RequestBody EmailVerifyRequestDto emailVerifyRequestDto){
        log.info("Verifying email to " + emailVerifyRequestDto.getEmail());
        boolean isVerified = emailService.verifyEmailCode(emailVerifyRequestDto);

        String result = isVerified ? "success" : "fail";
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "초기화된 비밀번호를 전송하는 API")
    @PostMapping("/send/reset")
    public ResponseEntity<String> sendPasswordResetEmail(@RequestParam String email) throws MessagingException {
        log.info("Sending password reset email to " + email);
        emailService.sendPasswordEmail(email);

        return ResponseEntity.ok("초기화된 비밀번호가 발송되었습니다.");
    }

}
