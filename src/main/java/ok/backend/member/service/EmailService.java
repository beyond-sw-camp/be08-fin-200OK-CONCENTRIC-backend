package ok.backend.member.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ok.backend.member.domain.entity.Email;
import ok.backend.member.domain.repository.EmailRepository;
import ok.backend.member.dto.EmailVerifyRequestDto;
import ok.backend.notification.domain.entity.Notification;
import ok.backend.notification.domain.entity.NotificationPending;
import ok.backend.notification.domain.enums.NotificationType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final EmailRepository emailRepository;

    private static final String senderEmail = "macleod.park@gmail.com";

    private final MemberService memberService;

    private final JavaMailSender javaMailSender;

    private String createCode(int targetStringLength) {
        int leftLimit = 48;
        int rightLimit = 122;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 | i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    private MimeMessage createVerificationEmailForm(String email) throws MessagingException {
        String authCode = createCode(6);

        MimeMessage message = javaMailSender.createMimeMessage();
        message.addRecipients(MimeMessage.RecipientType.TO, email);
        message.setSubject("안녕하세요. 인증번호입니다.");
        message.setFrom(senderEmail);
        message.setText("인증 코드 : " + authCode, "utf-8", "html");

        Email emailToSave = Email.builder()
                .email(email)
                .verificationCode(authCode)
                .build();

        emailRepository.save(emailToSave);
        Email found = emailRepository.findByEmail(email).orElse(null);
        if (found != null) {
            System.out.println(found.getEmail());
            System.out.println(found.getVerificationCode());
        }

        return message;
    }

    public void sendVerificationEmail(String toEmail) throws MessagingException {
        Email exist = emailRepository.findByEmail(toEmail).orElse(null);

        if(exist != null) {
            emailRepository.deleteByEmail(toEmail);
        }

        MimeMessage emailForm = createVerificationEmailForm(toEmail);

        javaMailSender.send(emailForm);
    }

    public Boolean verifyEmailCode(EmailVerifyRequestDto emailVerifyRequestDto) {
        Email found = emailRepository.findByEmail(emailVerifyRequestDto.getEmail()).orElse(null);
        log.info("code found by email: " + found);
        if (found == null) {
            return false;
        }
        return found.getVerificationCode().equals(emailVerifyRequestDto.getVerificationCode());
    }

    private MimeMessage createPasswordEmailForm(String email) throws MessagingException {
        String code = createCode(8);

        memberService.updatePassword(email, code);

        MimeMessage message = javaMailSender.createMimeMessage();
        message.addRecipients(MimeMessage.RecipientType.TO, email);
        message.setSubject("안녕하세요. 초기화된 비밀번호입니다.");
        message.setFrom(senderEmail);
        message.setText("새로운 비밀번호 : " + code, "utf-8", "html");

        return message;
    }

    public void sendPasswordEmail(String email) throws MessagingException {

        MimeMessage emailForm = createPasswordEmailForm(email);

        javaMailSender.send(emailForm);
    }

    public void sendNotificationEmail(NotificationPending notificationPending) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        message.addRecipients(MimeMessage.RecipientType.TO, notificationPending.getSchedule().getMember().getEmail());
        if(notificationPending.getNotificationType() == NotificationType.BEFORE_START_SCHEDULE){
            message.setSubject("일정이 곧 시작됩니다");
        }else{
            message.setSubject("일정이 곧 종료됩니다.");
        }
        message.setFrom(senderEmail);
        message.setText("알림이다!");

        javaMailSender.send(message);
    }

    public void sendInviteEmail(String recipientEmail, String inviteUrl) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        message.addRecipients(MimeMessage.RecipientType.TO, recipientEmail);

        message.setSubject("그룹에 초대합니다.");
        message.setFrom(senderEmail);
        message.setText("링크를 클릭하여 그룹에 참여하세요: " + inviteUrl); // HTML 형식으로 설정

        javaMailSender.send(message);
    }
}
