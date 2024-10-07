package ok.backend.team.service;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import ok.backend.common.exception.CustomException;
import ok.backend.common.exception.ErrorCode;
import ok.backend.common.security.util.SecurityUserDetailService;
import ok.backend.member.service.EmailService;
import ok.backend.team.domain.repository.TeamListRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamSendingService {

    private final EmailService emailService;
    private final TeamListRepository teamListRepository;
    private final SecurityUserDetailService securityUserDetailService;


    // 초대 URL 생성
    public String generateInviteUrl(Long teamId) {
        // 링크는 나중에 수정
        return "http://localhost:8080/invite?" + teamId;
    }

    // 초대 이메일 전송
    public void sendInviteEmail(Long teamId, String inviteeEmail) throws MessagingException {
        Long currentMemberId = securityUserDetailService.getLoggedInMember().getId();

        boolean isMember = teamListRepository.existsByTeamIdAndMemberId(teamId, currentMemberId);
        if (!isMember) {
            throw new CustomException(ErrorCode.NOT_ACCESS_TEAM);
        }

        String inviteUrl = generateInviteUrl(teamId);
        emailService.sendInviteEmail(inviteeEmail, inviteUrl);
    }

}