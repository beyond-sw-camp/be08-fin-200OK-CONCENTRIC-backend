package ok.backend.chat.service;

import ok.backend.chat.domain.entity.ChatRoom;
import ok.backend.chat.domain.entity.ChatRoomList;
import ok.backend.chat.domain.repository.ChatRoomListRepository;
import ok.backend.chat.domain.repository.ChatRoomRepository;
import ok.backend.chat.dto.req.ChatRoomListRequestDto;
import ok.backend.chat.dto.req.ChatRoomRequestDto;
import ok.backend.chat.dto.res.ChatRoomMemberResponseDto;
import ok.backend.chat.dto.res.ChatRoomListResponseDto;
import ok.backend.chat.dto.res.ChatRoomResponseDto;
import ok.backend.member.domain.entity.Member;
import ok.backend.member.domain.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChatService {

    @Autowired
    ChatRoomRepository chatRoomRepository;

    @Autowired
    ChatRoomListRepository chatRoomListRepository;

    @Autowired
    MemberRepository memberRepository;

    // TODO: memberID -> 토큰 인증, 단체 채팅방 생성

    // 단순 채팅방 생성
    public ChatRoomResponseDto createChat(Long memberId, ChatRoomRequestDto chatRoomRequestDto) {
        ChatRoom chatRoom = ChatRoom.builder()
                .name(chatRoomRequestDto.getName())
                .build();
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        Member member = memberRepository.findById(memberId).orElseThrow(()
                -> new IllegalArgumentException("Member not found"));

        ChatRoomList chatRoomList = ChatRoomList.builder()
                .member(member)
                .chatRoom(chatRoom)
                .bookmark(false)
                .nickname(chatRoom.getName())
                .build();
        chatRoomListRepository.save(chatRoomList);

        return new ChatRoomResponseDto(savedChatRoom);
    }

    // 채팅방 삭제
    public void deleteChat(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow(()
                -> new IllegalArgumentException("Chat room not found"));
        chatRoomRepository.delete(chatRoom);
    }

    // 채팅방 이름 수정
    public void renameChat(Long memberId, Long chatRoomId, ChatRoomListRequestDto chatRoomListRequestDto) {
        ChatRoomList chatRoomList = chatRoomListRepository.findByChatRoomIdAndMemberId(memberId, chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("Participant not found"));
        chatRoomList.setNickname(chatRoomListRequestDto.getNickname());
        chatRoomListRepository.save(chatRoomList);
    }

    // 채팅방 즐겨찾기 설정
    public void bookmarkChat(Long memberId, Long chatRoomId) {
        ChatRoomList chatRoomList = chatRoomListRepository.findByChatRoomIdAndMemberId(memberId, chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("Participant not found"));
        if (chatRoomList.getBookmark().equals(true)) {
            chatRoomList.setBookmark(false);
            chatRoomListRepository.save(chatRoomList);
        } else {
            chatRoomList.setBookmark(true);
            chatRoomListRepository.save(chatRoomList);
        }
    }

    // 단체 채팅방 참여
    public void joinChat(Long memberId, Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow(()
                -> new IllegalArgumentException("Chat room not found"));
        // TODO: 팀 조회해서 가입 여부 조회 로직 있어야하나..?
        Member member = memberRepository.findById(memberId).orElseThrow(()
                -> new IllegalArgumentException("Member not found"));
        ChatRoomList chatRoomList = ChatRoomList.builder()
                .member(member)
                .chatRoom(chatRoom)
                .bookmark(false)
                .nickname(chatRoom.getName())
                .build();
        chatRoomListRepository.save(chatRoomList);
    }

    // 단체 채팅방 나가기
    public void dropChat(Long memberId, Long chatRoomId) {
        ChatRoomList chatRoomList = chatRoomListRepository.findByChatRoomIdAndMemberId(memberId, chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("Participant not found"));

        chatRoomListRepository.delete(chatRoomList);
    }

    // 채팅방 참여자 조회
    public List<ChatRoomMemberResponseDto> findChatParticipant(Long memberId, Long chatRoomId) {
        if (chatRoomListRepository.findByChatRoomIdAndMemberId(memberId, chatRoomId).isPresent()) {
            List<ChatRoomList> chatRoomLists = chatRoomListRepository.findByChatRoomId(chatRoomId);
            return chatRoomLists.stream()
                    .map(chatRoomList -> new ChatRoomMemberResponseDto(
                            chatRoomList.getChatRoom().getId(),
                            chatRoomList.getMember().getId(),
                            chatRoomList.getMember().getNickname(),
                            chatRoomList.getMember().getImageUrl()
                    ))
                    .collect(Collectors.toList());
        } else return new ArrayList<>();
    }

    // 채팅방 목록 조회
    public List<ChatRoomListResponseDto> findChatRooms(Long memberId) {
        List<ChatRoomList> chatRoomLists = chatRoomListRepository.findByMemberId(memberId);

        return chatRoomLists.stream()
                .map(chatRoomList -> new ChatRoomListResponseDto(
                        chatRoomList.getChatRoom().getId(),
                        chatRoomList.getNickname(),
                        chatRoomList.getBookmark()
                ))
                .collect(Collectors.toList());
    }


}
