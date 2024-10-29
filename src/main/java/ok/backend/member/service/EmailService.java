package ok.backend.member.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ok.backend.member.domain.entity.Email;
import ok.backend.member.domain.repository.EmailRepository;
import ok.backend.member.dto.EmailVerifyRequestDto;
import ok.backend.notification.domain.entity.NotificationPending;
import ok.backend.notification.domain.enums.NotificationType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final EmailRepository emailRepository;

    private static final String senderEmail = "be08fin200okconcentric@gmail.com";

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
//        message.setFrom(senderEmail);
//        message.setText("인증 코드 : " + authCode, "utf-8", "html");
        String htmlContent = "<!DOCTYPE html>\n" +
                "<html lang=\"ko\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>인증 코드</title>\n" +
                "    <style>\n" +
                "        body {\n" +
                "            font-family: Arial, sans-serif;\n" +
                "            margin: 0;\n" +
                "            padding: 0;\n" +
                "        }\n" +
                "        .container {\n" +
                "            max-width: 600px;\n" +
                "            margin: 0 auto;\n" +
                "            background: #ffffff;\n" +
                "            border-radius: 8px;\n" +
                "            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);\n" +
                "        }\n" +
                "        .header {\n" +
                "            font-size: 30px;\n" +
                "            color: white;\n" +
                "            padding: 20px;\n" +
                "            text-align: center;\n" +
                "            border-radius: 8px 8px 0 0;\n" +
                "            background: linear-gradient(to right, #8A9BF9, #86EDDA);\n" +
                "        }\n" +
                "        .content {\n" +
                "            padding: 20px;\n" +
                "            line-height: 1.6;\n" +
                "        }\n" +
                "        .code {\n" +
                "            font-size: 20px; /* 폰트 크기를 줄였습니다. */\n" +
//                "            font-weight: bold;\n" +
                "            color: #000000;\n" +
                "            text-align: center;\n" +
                "            padding: 10px;\n" +
                "            background-color: #F5F5F5;\n" +
                "            border-radius: 5px;\n" +
                "            margin: 20px 0;\n" +
                "        }\n" +
                "        .footer {\n" +
                "            text-align: center;\n" +
                "            padding: 10px;\n" +
                "            font-size: 12px;\n" +
                "            color: #888;\n" +
                "            border-top: 1px solid #e0e0e0;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "\n" +
                "<div class=\"container\">\n" +
                "    <div class=\"header\">\n" +
                "        CONCENTRIC\n" +
                "    </div>\n" +
                "    <div class=\"content\">\n" +
                "        <p>안녕하세요!</p>\n" +
                "        <p>가입해 주셔서 감사합니다. 다음 인증 코드를 사용하여 등록을 완료하세요:</p>\n" +
                "        <div class=\"code\">123456</div>\n" +
                "        <p>이 코드는 10분 후에 만료됩니다.</p>\n" +
                "        <p>이 요청을 하지 않으셨다면 이 이메일을 무시하시기 바랍니다.</p>\n" +
                "    </div>\n" +
                "    <div class=\"footer\">\n" +
                "        <p>저희와 함께해 주셔서 감사합니다!</p>\n" +
                "        <p>회사 이름 | <a href=\"#\">개인정보 처리방침</a></p>\n" +
                "    </div>\n" +
                "</div>\n" +
                "\n" +
                "</body>\n" +
                "</html>\n";


        htmlContent = htmlContent.replace("123456",authCode);
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setText(htmlContent, true);
//        message.setContent(htmlContent, "text/html");
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
        memberService.findMemberByToEmail(toEmail);

        emailRepository.findByEmail(toEmail).ifPresent(exist -> emailRepository.deleteByEmail(toEmail));

        MimeMessage emailForm = createVerificationEmailForm(toEmail);

        javaMailSender.send(emailForm);
    }

    public Boolean verifyEmailCode(EmailVerifyRequestDto emailVerifyRequestDto) {
        Email found = emailRepository.findByEmail(emailVerifyRequestDto.getEmail()).orElse(null);
        log.info("code found by email: " + found.getVerificationCode());
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

        message.setSubject("당신을 그룹에 초대합니다.");
        message.setFrom(senderEmail);

        // HTML 콘텐츠 생성
        String htmlContent = "<!DOCTYPE html>\n" +
                "<html lang=\"ko\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>CONCENTRIC</title>\n" +
                "    <style>\n" +
                "        body {\n" +
                "            font-family: Arial, sans-serif;\n" +
                "            margin: 0;\n" +
                "            padding: 0;\n" +
                "        }\n" +
                "        .container {\n" +
                "            max-width: 600px;\n" +
                "            margin: 0 auto;\n" +
                "            background: #ffffff;\n" +
                "            border-radius: 8px;\n" +
                "            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);\n" +
                "        }\n" +
                "        .header {\n" +
                "            font-size: 30px;\n" +
                "            color: white;\n" +
                "            padding: 20px;\n" +
                "            text-align: center;\n" +
                "            border-radius: 8px 8px 0 0;\n" +
                "            background: linear-gradient(to right, #8A9BF9, #86EDDA);\n" +
                "        }\n" +
                "        .content {\n" +
                "            padding: 20px;\n" +
                "            line-height: 1.6;\n" +
                "        }\n" +
                "        .invite-link {\n" +
                "            font-size: 20px;\n" +
                "            color: #000000;\n" +
                "            text-align: center;\n" +
                "            padding: 10px;\n" +
                "            background-color: #F5F5F5;\n" +
                "            border-radius: 5px;\n" +
                "            margin: 20px 0;\n" +
                "            display: inline-block;\n" +
                "            text-decoration: none;\n" +
                "            border: 1px solid #8A9BF9;\n" +
                "        }\n" +
                "        .footer {\n" +
                "            text-align: center;\n" +
                "            padding: 10px;\n" +
                "            font-size: 12px;\n" +
                "            color: #888;\n" +
                "            border-top: 1px solid #e0e0e0;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<div class=\"container\">\n" +
                "    <div class=\"header\">\n" +
                "        CONCENTRIC\n" +
                "    </div>\n" +
                "    <div class=\"content\">\n" +
                "        <p>안녕하세요!</p>\n" +
                "        <p>당신은 새로운 그룹에 초대되었습니다.</p>\n" +
                "        <p>아래 링크를 클릭하여 그룹에 참여하세요:</p>\n" +
                "        <a href=\"" + inviteUrl + "\" class=\"invite-link\">그룹 참여하기</a>\n" +
                "        <p>이 요청을 하지 않으셨다면 이 이메일을 무시하시기 바랍니다.</p>\n" +
                "    </div>\n" +
                "    <div class=\"footer\">\n" +
                "        <p>저희와 함께해 주셔서 감사합니다!</p>\n" +
                "        <p>회사 이름 | <a href=\"#\">개인정보 처리방침</a></p>\n" +
                "    </div>\n" +
                "</div>\n" +
                "</body>\n" +
                "</html>\n";

        // HTML 메시지로 설정
        message.setContent(htmlContent, "text/html; charset=UTF-8");

        // 메일 전송
        javaMailSender.send(message);
    }

}
