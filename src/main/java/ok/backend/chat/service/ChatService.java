package ok.backend.chat.service;

import lombok.extern.slf4j.Slf4j;
import ok.backend.chat.domain.entity.ChatRoom;
import ok.backend.chat.domain.entity.ChatRoomList;
import ok.backend.chat.domain.entity.Status;
import ok.backend.chat.domain.repository.ChatRoomListRepository;
import ok.backend.chat.domain.repository.ChatRoomRepository;
import ok.backend.chat.dto.req.ChatRoomListRequestDto;
import ok.backend.chat.dto.req.ChatRoomRequestDto;
import ok.backend.chat.dto.res.ChatRoomMemberResponseDto;
import ok.backend.chat.dto.res.ChatRoomListResponseDto;
import ok.backend.chat.dto.res.ChatRoomResponseDto;
import ok.backend.common.exception.CustomException;
import ok.backend.common.security.util.SecurityUserDetailService;
import ok.backend.member.domain.entity.Member;
import ok.backend.member.domain.repository.MemberRepository;
import ok.backend.team.domain.entity.Team;
import ok.backend.team.domain.entity.TeamList;
import ok.backend.team.domain.repository.TeamListRepository;
import ok.backend.team.domain.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static ok.backend.common.exception.ErrorCode.*;

@Slf4j
@Service
@Transactional
public class ChatService {

    @Autowired
    ChatRoomRepository chatRoomRepository;

    @Autowired
    ChatRoomListRepository chatRoomListRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    TeamListRepository teamListRepository;

    private final SecurityUserDetailService securityUserDetailService;

    public ChatService(SecurityUserDetailService securityUserDetailService) {
        this.securityUserDetailService = securityUserDetailService;
    }

    // 개인 채팅방 생성
    public ChatRoomResponseDto createChat(Long friendId, ChatRoomRequestDto chatRoomRequestDto) {

        Member member = memberRepository.findById(securityUserDetailService.getLoggedInMember().getId()).orElseThrow(()
                -> new CustomException(MEMBER_NOT_FOUND));

        Member friend = memberRepository.findById(friendId).orElseThrow(()
                -> new CustomException(MEMBER_NOT_FOUND));

        if (member.getId().equals(friendId)) {
            throw new CustomException(INVALID_CHAT_REQUEST);
        }

        List<ChatRoomList> memberChatRoomLists = chatRoomListRepository.findByMemberId(member.getId());

        for (ChatRoomList chatRoomList : memberChatRoomLists) {
            if (chatRoomList.getChatRoom().getChatRoomList().stream()
                    .anyMatch(crList -> crList.getMember().equals(friend) && crList.getChatRoom().getStatus() == Status.P)) {
                throw new CustomException(DUPLICATE_CHAT);
            }
        }

        ChatRoom chatRoom = ChatRoom.createChatRoom(chatRoomRequestDto.getName(), Status.P);
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        ChatRoomList chatRoomListMember = ChatRoomList.createChatRoomList(member, savedChatRoom);
        ChatRoomList chatRoomListFriend = ChatRoomList.createChatRoomList(friend, savedChatRoom);
        chatRoomListRepository.save(chatRoomListMember);
        chatRoomListRepository.save(chatRoomListFriend);

        return new ChatRoomResponseDto(savedChatRoom);
    }

    //     단체 채팅방 생성
    public ChatRoomResponseDto createTeamChat(Long teamId) {
        Team team = teamRepository.findById(teamId).orElseThrow(()
                -> new CustomException(TEAM_NOT_FOUND));

        ChatRoom chatRoom = ChatRoom.createChatRoom(team.getName(), Status.T);
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        List<TeamList> teamMember = teamListRepository.findByTeamId(teamId);

        // 각 멤버를 ChatRoomList에 저장
        for (TeamList teamList : teamMember) {
            ChatRoomList chatRoomList = ChatRoomList.createChatRoomList(teamList.getMember(), savedChatRoom);
            chatRoomListRepository.save(chatRoomList);
        }
        return new ChatRoomResponseDto(savedChatRoom);
    }

    // 채팅방 삭제(그룹 삭제 시)
    public void deleteChat(Long chatRoomId) {
        // TODO: 팀 테이블 가져와서 생성 회원 ID가 memberId와 일치할 경우, 삭제 or NOT_ACCESS_CHAT
        Member member = memberRepository.findById(securityUserDetailService.getLoggedInMember().getId()).orElseThrow(()
                -> new CustomException(MEMBER_NOT_FOUND));
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow(()
                -> new CustomException(CHAT_NOT_FOUND));
        chatRoomRepository.delete(chatRoom);
    }

    // 채팅방 이름 수정
    public void renameChat(Long chatRoomId, ChatRoomListRequestDto chatRoomListRequestDto) {
        if (chatRoomListRequestDto.getNickname().isEmpty()) {
            throw new CustomException(EMPTY_INPUT_CHAT);
        }
        ChatRoomList chatRoomList = chatRoomListRepository.findByMemberIdAndChatRoomId(
                        securityUserDetailService.getLoggedInMember().getId(), chatRoomId)
                .orElseThrow(() -> new CustomException(NOT_ACCESS_CHAT));
        chatRoomList.updateNickname(chatRoomListRequestDto);
        chatRoomListRepository.save(chatRoomList);
    }

    // 채팅방 즐겨찾기 설정
    public void bookmarkChat(Long chatRoomId) {
        ChatRoomList chatRoomList = chatRoomListRepository.findByMemberIdAndChatRoomId(
                        securityUserDetailService.getLoggedInMember().getId(), chatRoomId)
                .orElseThrow(() -> new CustomException(NOT_ACCESS_CHAT));
        if (chatRoomList.getBookmark().equals(true)) {
            chatRoomList.updateBookmark(false);
            chatRoomListRepository.save(chatRoomList);
        } else {
            chatRoomList.updateBookmark(true);
            chatRoomListRepository.save(chatRoomList);
        }
    }

    // 단체 채팅방 참여
    public void joinChat(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow(()
                -> new CustomException(CHAT_NOT_FOUND));
        Member member = memberRepository.findById(securityUserDetailService.getLoggedInMember().getId()).orElseThrow(()
                -> new CustomException(MEMBER_NOT_FOUND));
        ChatRoomList chatRoomList = ChatRoomList.createChatRoomList(member, chatRoom);

        chatRoomListRepository.save(chatRoomList);
    }

    // 단체 채팅방 나가기
    public void dropChat(Long chatRoomId) {

        ChatRoomList chatRoomList = chatRoomListRepository.findByMemberIdAndChatRoomId(
                        securityUserDetailService.getLoggedInMember().getId(), chatRoomId)
                .orElseThrow(() -> new CustomException(NOT_ACCESS_CHAT));

        chatRoomListRepository.delete(chatRoomList);
    }

    // 채팅방 참여자 조회
    public List<ChatRoomMemberResponseDto> findChatParticipant(Long chatRoomId) {
        if (chatRoomListRepository.findByMemberIdAndChatRoomId(
                securityUserDetailService.getLoggedInMember().getId(), chatRoomId).isPresent()) {
            List<ChatRoomList> chatRoomLists = chatRoomListRepository.findByChatRoomId(chatRoomId);
            return chatRoomLists.stream()
                    .map(chatRoomList -> new ChatRoomMemberResponseDto(
                            chatRoomList.getChatRoom().getId(),
                            chatRoomList.getMember().getId(),
                            chatRoomList.getMember().getNickname(),
                            chatRoomList.getMember().getImageUrl()
                    ))
                    .collect(Collectors.toList());
        } else throw new CustomException(NOT_ACCESS_CHAT);
    }

    // 채팅방 목록 조회
    public List<ChatRoomListResponseDto> findChatRooms() {
        if (chatRoomListRepository.findByMemberId(securityUserDetailService.getLoggedInMember().getId()).isEmpty()) {
            throw new CustomException(NOT_ACCESS_CHAT);
        }
        List<ChatRoomList> chatRoomLists = chatRoomListRepository.findByMemberId(
                securityUserDetailService.getLoggedInMember().getId());

        return chatRoomLists.stream()
                .map(chatRoomList -> new ChatRoomListResponseDto(
                        chatRoomList.getChatRoom().getId(),
                        chatRoomList.getNickname(),
                        chatRoomList.getBookmark()
                ))
                .collect(Collectors.toList());
    }
}