package ok.backend.member.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ok.backend.member.domain.entity.Email;
import ok.backend.member.domain.entity.Member;
import ok.backend.member.domain.repository.EmailRepository;
import ok.backend.member.dto.EmailVerifyRequestDto;
import ok.backend.notification.domain.entity.NotificationPending;
import ok.backend.notification.domain.enums.NotificationType;
import ok.backend.schedule.domain.entity.Schedule;
import ok.backend.schedule.domain.entity.SubSchedule;
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
                "</head>\n" +
                "<body style=\"font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f9f9f9;\">\n" +
                "<div style=\"max-width: 600px; margin: 20px auto; background: #ffffff; border-radius: 8px; border: 1px solid #e0e0e0; box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);\">\n" +
                "    <div style=\"font-size: 30px; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; background: linear-gradient(to right, #8A9BF9, #86EDDA);\">\n" +
                "        CONCENTRIC\n" +
                "    </div>\n" +
                "    <div style=\"padding: 30px; line-height: 1.6; color: #333333;\">\n" +
                "        <p>안녕하세요!</p>\n" +
                "        <p>가입해 주셔서 감사합니다. 다음 인증 코드를 사용하여 등록을 완료하세요:</p>\n" +
                "        <div style=\"font-size: 20px; color: #000000; text-align: center; padding: 10px; background-color: #F5F5F5; border-radius: 5px; margin: 20px 0;\">\n" +
                "            " + authCode + "\n" +
                "        </div>\n" +
                "        <p>이 코드는 10분 후에 만료됩니다.</p>\n" +
                "        <p>이 요청을 하지 않으셨다면 이 이메일을 무시하시기 바랍니다.</p>\n" +
                "    </div>\n" +
                "    <div style=\"text-align: center; padding: 10px; font-size: 12px; color: #888888; border-top: 1px solid #e0e0e0;\">\n" +
                "        <p>저희와 함께해 주셔서 감사합니다!</p>\n" +
                "        <p>CONCENTRIC | <a href=\"#\" style=\"color: #8A9BF9; text-decoration: none;\">개인정보 처리방침</a></p>\n" +
                "    </div>\n" +
                "</div>\n" +
                "</body>\n" +
                "</html>";


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

        String htmlContent = "<!DOCTYPE html>\n" +
                "<html lang=\"ko\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>초기화된 비밀번호</title>\n" +
                "</head>\n" +
                "<body style=\"font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f9f9f9;\">\n" +
                "<div style=\"max-width: 600px; margin: 20px auto; background: #ffffff; border-radius: 8px; border: 1px solid #e0e0e0; box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);\">\n" +
                "    <div style=\"font-size: 30px; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; background: linear-gradient(to right, #8A9BF9, #86EDDA);\">\n" +
                "        CONCENTRIC\n" +
                "    </div>\n" +
                "    <div style=\"padding: 30px; line-height: 1.6; color: #333333;\">\n" +
                "        <p>안녕하세요!</p>\n" +
                "        <p>요청하신 초기화된 비밀번호는 아래와 같습니다:</p>\n" +
                "        <div style=\"font-size: 20px; color: #000000; text-align: center; padding: 10px; background-color: #F5F5F5; border-radius: 5px; margin: 20px 0;\">\n" +
                "            " + code + "\n" +
                "        </div>\n" +
                "        <p>로그인 후 비밀번호를 변경하는 것을 권장드립니다.</p>\n" +
                "        <p>이 요청을 하지 않으셨다면 이 이메일을 무시하시기 바랍니다.</p>\n" +
                "    </div>\n" +
                "    <div style=\"text-align: center; padding: 10px; font-size: 12px; color: #888888; border-top: 1px solid #e0e0e0;\">\n" +
                "        <p>저희와 함께해 주셔서 감사합니다!</p>\n" +
                "        <p>CONCENTRIC | <a href=\"#\" style=\"color: #8A9BF9; text-decoration: none;\">개인정보 처리방침</a></p>\n" +
                "    </div>\n" +
                "</div>\n" +
                "</body>\n" +
                "</html>";

        message.setContent(htmlContent, "text/html; charset=utf-8");

        return message;
    }

    public void sendPasswordEmail(String email) throws MessagingException {

        MimeMessage emailForm = createPasswordEmailForm(email);

        javaMailSender.send(emailForm);
    }

    public void sendNotificationEmail(NotificationPending notificationPending) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        message.addRecipients(MimeMessage.RecipientType.TO, notificationPending.getSchedule().getMember().getEmail());

        String subject;
        String notificationMessage;

        if(notificationPending.getNotificationType().equals(NotificationType.BEFORE_START_SCHEDULE)){
            subject = "일정이 곧 시작됩니다!";
            notificationMessage = "곧 시작할 예정인 일정이 있으니 준비해 주세요!";
        }else {
            subject = "일정이 곧 종료됩니다!";
            notificationMessage = "곧 종료될 일정이 있으니 마무리 준비를 해 주세요!";

        }
        message.setSubject(subject);
        message.setFrom(senderEmail);

        String htmlContent = "<!DOCTYPE html>\n" +
                "<html lang=\"ko\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>일정 알림</title>\n" +
                "</head>\n" +
                "<body style=\"font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f9f9f9;\">\n" +
                "<div style=\"max-width: 600px; margin: 20px auto; background: #ffffff; border-radius: 8px; border: 1px solid #e0e0e0; box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);\">\n" +
                "    <div style=\"font-size: 30px; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; background: linear-gradient(to right, #8A9BF9, #86EDDA);\">\n" +
                "        CONCENTRIC 일정 알림\n" +
                "    </div>\n" +
                "    <div style=\"padding: 30px; line-height: 1.6; color: #333333;\">\n" +
                "        <p>안녕하세요!</p>\n" +
                "        <p>" + notificationMessage + "</p>\n" +
                "        <p>일정 시간에 맞춰 준비하시기 바랍니다.</p>\n" +
                "        <p>이 알림을 받지 않으셔야 한다면, 설정에서 알림을 비활성화해 주세요.</p>\n" +
                "    </div>\n" +
                "    <div style=\"text-align: center; padding: 10px; font-size: 12px; color: #888888; border-top: 1px solid #e0e0e0;\">\n" +
                "        <p>저희와 함께해 주셔서 감사합니다!</p>\n" +
                "        <p>CONCENTRIC | <a href=\"#\" style=\"color: #8A9BF9; text-decoration: none;\">개인정보 처리방침</a></p>\n" +
                "    </div>\n" +
                "</div>\n" +
                "</body>\n" +
                "</html>";

        message.setContent(htmlContent, "text/html; charset=utf-8");

        javaMailSender.send(message);
    }

    public void sendNotificationEmailFromSchedule(Schedule schedule, Member member) throws MessagingException {

    }

    public void sendNotificationEmailFromSubSchedule(SubSchedule subSchedule, Member member) throws MessagingException {

    }

    public void sendInviteEmail(String recipientEmail, String inviteUrl) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        message.addRecipients(MimeMessage.RecipientType.TO, recipientEmail);

        message.setSubject("당신을 그룹에 초대합니다.");
        message.setFrom(senderEmail);

        String htmlContent = "<!DOCTYPE html>\n" +
                "<html lang=\"ko\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>CONCENTRIC</title>\n" +
                "</head>\n" +
                "<body style=\"font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f9f9f9;\">\n" +
                "<div style=\"max-width: 600px; margin: 20px auto; background: #ffffff; border-radius: 8px; border: 1px solid #e0e0e0; box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);\">\n" +
                "    <div style=\"font-size: 30px; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; background: linear-gradient(to right, #8A9BF9, #86EDDA);\">\n" +
                "        CONCENTRIC\n" +
                "    </div>\n" +
                "    <div style=\"padding: 30px; line-height: 1.6; color: #333333;\">\n" +
                "        <p>안녕하세요!</p>\n" +
                "        <p>당신은 새로운 그룹에 초대되었습니다.</p>\n" +
                "        <p>아래 링크를 클릭하여 그룹에 참여하세요:</p>\n" +
                "        <a href=\"" + inviteUrl + "\" style=\"font-size: 20px; color: #0052CC; text-align: center; padding: 10px; background-color: #F5F5F5; border-radius: 5px; text-decoration: none; display: inline-block; margin: 20px 0; border: 1px solid #8A9BF9;\">그룹 참여하기</a>\n" +
                "        <p>이 요청을 하지 않으셨다면 이 이메일을 무시하시기 바랍니다.</p>\n" +
                "    </div>\n" +
                "    <div style=\"text-align: center; padding: 10px; font-size: 12px; color: #888888; border-top: 1px solid #e0e0e0;\">\n" +
                "        <p>저희와 함께해 주셔서 감사합니다!</p>\n" +
                "        <p>CONCENTRIC | <a href=\"#\" style=\"color: #8A9BF9; text-decoration: none;\">개인정보 처리방침</a></p>\n" +
                "    </div>\n" +
                "</div>\n" +
                "</body>\n" +
                "</html>";

        // HTML 메시지로 설정
        message.setContent(htmlContent, "text/html; charset=UTF-8");

        // 메일 전송
        javaMailSender.send(message);
    }

}
