package ok.backend.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ok.backend.chat.domain.entity.ChatRoom;
import ok.backend.chat.domain.entity.ChatRoomList;
import ok.backend.chat.domain.entity.Type;
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
import ok.backend.member.service.MemberService;
import ok.backend.storage.service.StorageFileService;
import ok.backend.storage.service.StorageService;
import ok.backend.team.domain.entity.Team;
import ok.backend.team.domain.entity.TeamList;
import ok.backend.team.service.TeamService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ok.backend.common.exception.ErrorCode.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomListRepository chatRoomListRepository;
    private final MemberService memberService;
    private final TeamService teamService;
    private final SecurityUserDetailService securityUserDetailService;
    private final StorageService storageService;
    private final StorageFileService storageFileService;


    // 개인 채팅방 생성
    public ChatRoomResponseDto createChat(Long friendId, ChatRoomRequestDto chatRoomRequestDto) {

        Member member = memberService.findMemberById(securityUserDetailService.getLoggedInMember().getId());
        Member friend = memberService.findMemberById(friendId);

        if (member.getId().equals(friendId)) {
            throw new CustomException(INVALID_CHAT_REQUEST);
        }

        List<ChatRoomList> memberChatRoomLists = chatRoomListRepository.findByMemberId(member.getId());

        for (ChatRoomList chatRoomList : memberChatRoomLists) {
            if (chatRoomList.getChatRoom().getChatRoomList().stream()
                    .anyMatch(crList -> crList.getMember().equals(friend) && crList.getChatRoom().getType() == Type.P)) {
                throw new CustomException(DUPLICATE_CHAT);
            }
        }

        ChatRoom chatRoom = ChatRoom.createChatRoom(chatRoomRequestDto.getName(), Type.P, null, true);
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        chatRoomListRepository.save(ChatRoomList.createChatRoomList(member, savedChatRoom));
        chatRoomListRepository.save(ChatRoomList.createChatRoomList(friend, savedChatRoom));

        storageService.createChatStorage(chatRoom.getId());

        ChatRoomResponseDto responseDto = new ChatRoomResponseDto(savedChatRoom);
        System.out.println("Response DTO: " + responseDto);
        return responseDto;
//        return new ChatRoomResponseDto(savedChatRoom);
    }

    // 단체 채팅방 생성(팀 서비스)
    public void createTeamChat(Long teamId) {
        Team team = teamService.findById(teamId);

        ChatRoom chatRoom = ChatRoom.createChatRoom(team.getName(), Type.T, teamId, true);
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        List<TeamList> teamMember = teamService.findByTeamId(teamId);

        // 각 멤버를 ChatRoomList에 저장
        for (TeamList teamList : teamMember) {
            ChatRoomList chatRoomList = ChatRoomList.createChatRoomList(teamList.getMember(), savedChatRoom);
            chatRoomListRepository.save(chatRoomList);
        }

        storageService.createChatStorage(savedChatRoom.getId());
    }

    // 채팅방 삭제(팀 서비스)
    public void deleteChat(Long teamId) {
        Member member = memberService.findMemberById(securityUserDetailService.getLoggedInMember().getId());
        ChatRoom chatRoom = chatRoomRepository.findByTeamId(teamId).orElseThrow(()
                -> new CustomException(CHAT_NOT_FOUND));
        Team team = teamService.findById(teamId);
        if (member.getId().equals(team.getCreatorId())) {
            chatRoomRepository.delete(chatRoom);
        }
        else throw new CustomException(NOT_ACCESS_CHAT);

        storageService.deleteChatStorage(chatRoom.getId());
    }

    // 채팅방 이름 수정
    public void renameChat(ChatRoomListRequestDto chatRoomListRequestDto) {
        if (chatRoomListRequestDto.getNickname().isEmpty()) {
            throw new CustomException(EMPTY_INPUT_CHAT);
        }
        ChatRoomList chatRoomList = chatRoomListRepository.findByMemberIdAndChatRoomId(
                        securityUserDetailService.getLoggedInMember().getId(), chatRoomListRequestDto.getChatRoomId())
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

    // 단체 채팅방 참여(팀 서비스)
    public void joinChat(Long teamId, Member member) {
        ChatRoom chatRoom = chatRoomRepository.findByTeamId(teamId).orElseThrow(()
                -> new CustomException(CHAT_NOT_FOUND));
        ChatRoomList chatRoomList = ChatRoomList.createChatRoomList(member, chatRoom);

        chatRoomListRepository.save(chatRoomList);
    }

    // 채팅방 나가기(팀 탈퇴 시: 리스트 삭제, 회원 탈퇴 시: 비활성화)
    public void dropChat(Long chatRoomId) {

        ChatRoomList chatRoomList = chatRoomListRepository.findByMemberIdAndChatRoomId(
                        securityUserDetailService.getLoggedInMember().getId(), chatRoomId)
                .orElseThrow(() -> new CustomException(NOT_ACCESS_CHAT));

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow(()
                -> new CustomException(CHAT_NOT_FOUND));

        if (chatRoom.getType().equals(Type.T)) {
            Team team = teamService.findById(chatRoom.getTeamId());
            Long creatorId = securityUserDetailService.getLoggedInMember().getId();
            if (team.getCreatorId().equals(creatorId)) {
                throw new CustomException(INVALID_DELETE_REQUEST);
            }
            chatRoomListRepository.delete(chatRoomList);
        } else {
            chatRoom.updateIsActive(false);
            chatRoomRepository.save(chatRoom);
            chatRoomListRepository.delete(chatRoomList);
        }
    }

    // 채팅방 나가기(팀 탈퇴 시: 리스트 삭제, 회원 탈퇴 시: 비활성화)
    public void dropTeamChat(Long memberId, Long chatRoomId) {

        ChatRoomList chatRoomList = chatRoomListRepository.findByMemberIdAndChatRoomId(memberId, chatRoomId)
                .orElseThrow(() -> new CustomException(NOT_ACCESS_CHAT));

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow(()
                -> new CustomException(CHAT_NOT_FOUND));

        Team team = teamService.findById(chatRoom.getTeamId());
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
        if (securityUserDetailService.getLoggedInMember().getId() == null) {
            throw new CustomException(UNAUTHORIZED);
        }
        List<ChatRoomList> chatRoomLists = chatRoomListRepository.findByMemberIdAndChatRoomIsActiveTrue(
                securityUserDetailService.getLoggedInMember().getId());

        List<ChatRoomListResponseDto> chatRoomListResponseDtos = new ArrayList<>();

        for (ChatRoomList chatRoomList : chatRoomLists) {
            if (chatRoomList.getChatRoom().getType().equals(Type.T)) {
                Team team = teamService.findById(chatRoomList.getChatRoom().getTeamId());
                ChatRoomListResponseDto chatRoomListResponseDto = new ChatRoomListResponseDto(
                        chatRoomList, team.getName(), team.getImageUrl()
                );
                chatRoomListResponseDtos.add(chatRoomListResponseDto);
            } else {
                List<ChatRoomList> chatRoomListFriend = chatRoomListRepository
                        .findByChatRoomIdAndMemberIdNot(chatRoomList.getChatRoom().getId(), chatRoomList.getMember().getId());
                Member friend = memberService.findMemberById(chatRoomListFriend.get(0).getMember().getId());
                ChatRoomListResponseDto chatRoomListResponseDto = new ChatRoomListResponseDto(
                        chatRoomList, friend.getNickname(), friend.getImageUrl()
                );
                chatRoomListResponseDtos.add(chatRoomListResponseDto);
            }
        }
        return chatRoomListResponseDtos;
    }

    // 채팅방 권한 확인
    public void authMember(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow(()
                -> new CustomException(CHAT_NOT_FOUND));
        Member member = memberService.findMemberById(securityUserDetailService.getLoggedInMember().getId());
        chatRoomListRepository.findByMemberIdAndChatRoomId(
                securityUserDetailService.getLoggedInMember().getId(), chatRoomId).orElseThrow(
                () -> new CustomException(NOT_ACCESS_CHAT));

        if (chatRoom.getType().equals(Type.P)) {
            List<ChatRoomList> chatRoomLists = chatRoomListRepository
                    .findByChatRoomIdAndMemberIdNot(chatRoomId, member.getId());

            for (ChatRoomList chatRoomList : chatRoomLists) {
                Member otherMember = memberService.findMemberById(chatRoomList.getMember().getId());
                // 이 상태로는 상대방이 탈퇴할 경우 채팅방 조회 불가능
                // 지금은 오류를 던져주는데 이거 말고 프론트 구현되면 채팅을 막는 방법을 찾아야할듯
                if (otherMember.getIsActive().equals(false)) {
                    throw new CustomException(MEMBER_DELETED);
                }
            }
        }

    }

    public ChatRoom findByTeamId(Long teamId) {
        return chatRoomRepository.findByTeamId(teamId).orElseThrow(()
                -> new CustomException(CHAT_NOT_FOUND));
    }
}