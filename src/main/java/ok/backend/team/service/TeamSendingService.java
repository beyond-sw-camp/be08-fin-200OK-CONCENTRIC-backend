package ok.backend.team.service;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import ok.backend.member.service.EmailService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamSendingService {

    private final EmailService emailService;


    // 초대 URL 생성
    public String generateInviteUrl(Long teamId) {
        // 임시 링크
        return "http://localhost:8080/invite?teamId=" + teamId;
    }

    // 초대 이메일 전송
    public void sendInviteEmail(Long teamId, String inviteeEmail) throws MessagingException {
        String inviteUrl = generateInviteUrl(teamId);
        emailService.sendInviteEmail(inviteeEmail, inviteUrl);
    }
}