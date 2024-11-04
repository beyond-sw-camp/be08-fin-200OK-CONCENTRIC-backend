package ok.backend.team.service;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import ok.backend.common.exception.CustomException;
import ok.backend.common.exception.ErrorCode;
import ok.backend.common.security.util.SecurityUserDetailService;
import ok.backend.member.domain.entity.Invite;
import ok.backend.member.domain.entity.Member;
import ok.backend.member.service.EmailService;
import ok.backend.member.service.InviteService;
import ok.backend.member.service.MemberService;
import ok.backend.team.domain.repository.TeamListRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamSendingService {

    private final EmailService emailService;
    private final TeamListRepository teamListRepository;
    private final SecurityUserDetailService securityUserDetailService;
    private final MemberService memberService;
    private final InviteService inviteService;


    // 초대 URL 생성
    public String generateInviteUrl(Long teamId, String email) {
        // 링크는 나중에 수정
        String uuid = UUID.randomUUID().toString();
        Invite invite = Invite.builder()
                .id(uuid)
                .teamId(teamId)
                .email(email)
                .build();
        inviteService.saveInvite(invite);
        return "http://localhost:8080/v1/api/invite/accept/" + "key=" + uuid + "&" + "teamId=" + teamId + "&" + "email=" + email;
    }

    // 초대 이메일 전송
    public void sendInviteEmail(Long teamId, Long receiverId) throws MessagingException {
        Long currentMemberId = securityUserDetailService.getLoggedInMember().getId();

        boolean isMember = teamListRepository.existsByTeamIdAndMemberId(teamId, currentMemberId);
        if (!isMember) {
            throw new CustomException(ErrorCode.NOT_ACCESS_TEAM);
        }

        Member receiver = memberService.findMemberById(receiverId);

        String inviteUrl = generateInviteUrl(teamId, receiver.getEmail());
        emailService.sendInviteEmail(receiver.getEmail(), inviteUrl);
    }

}