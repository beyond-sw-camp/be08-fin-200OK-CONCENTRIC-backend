package ok.backend.member.service;

import lombok.RequiredArgsConstructor;
import ok.backend.common.exception.CustomException;
import ok.backend.common.exception.ErrorCode;
import ok.backend.member.domain.entity.Invite;
import ok.backend.member.domain.repository.InviteRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InviteService {

    private final InviteRepository inviteRepository;

    public void saveInvite(Invite invite) {
        inviteRepository.findById(invite.getId()).ifPresent(inviteRepository::delete);
        inviteRepository.save(invite);
    }

    public Invite findById(String id){
        return inviteRepository.findById(id).orElseThrow(() ->
                new CustomException(ErrorCode.INVITE_NOT_FOUND));
    }
}
