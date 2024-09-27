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
import ok.backend.common.exception.CustomException;
import ok.backend.member.domain.entity.Member;
import ok.backend.member.domain.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ok.backend.common.exception.ErrorCode.*;

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
                -> new CustomException(MEMBER_NOT_FOUND));

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
                -> new CustomException(CHAT_NOT_FOUND));
        chatRoomRepository.delete(chatRoom);
    }

    // 채팅방 이름 수정
    public void renameChat(Long memberId, Long chatRoomId, ChatRoomListRequestDto chatRoomListRequestDto) {
        ChatRoomList chatRoomList = chatRoomListRepository.findByMemberIdAndChatRoomId(memberId, chatRoomId)
                .orElseThrow(() -> new CustomException(NOT_ACCESS_CHAT));
        chatRoomList.setNickname(chatRoomListRequestDto.getNickname());
        chatRoomListRepository.save(chatRoomList);
    }

    // 채팅방 즐겨찾기 설정
    public void bookmarkChat(Long memberId, Long chatRoomId) {
        ChatRoomList chatRoomList = chatRoomListRepository.findByMemberIdAndChatRoomId(memberId, chatRoomId)
                .orElseThrow(() -> new CustomException(NOT_ACCESS_CHAT));
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
                -> new CustomException(CHAT_NOT_FOUND));
        // TODO: 팀 조회해서 가입 여부 조회 로직 있어야하나..?
        Member member = memberRepository.findById(memberId).orElseThrow(()
                -> new CustomException(MEMBER_NOT_FOUND));
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
        ChatRoomList chatRoomList = chatRoomListRepository.findByMemberIdAndChatRoomId(memberId, chatRoomId)
                .orElseThrow(() -> new CustomException(NOT_ACCESS_CHAT));

        chatRoomListRepository.delete(chatRoomList);
    }

    // 채팅방 참여자 조회
    public List<ChatRoomMemberResponseDto> findChatParticipant(Long memberId, Long chatRoomId) {
        if (chatRoomListRepository.findByMemberIdAndChatRoomId(memberId, chatRoomId).isPresent()) {
            List<ChatRoomList> chatRoomLists = chatRoomListRepository.findByChatRoomId(chatRoomId);
            return chatRoomLists.stream()
                    .map(chatRoomList -> new ChatRoomMemberResponseDto(
                            chatRoomList.getChatRoom().getId(),
                            chatRoomList.getMember().getId(),
                            chatRoomList.getMember().getNickname(),
                            chatRoomList.getMember().getImageUrl()
                    ))
                    .collect(Collectors.toList());
        } else throw new CustomException(MEMBER_NOT_FOUND);
    }

    // 채팅방 목록 조회
    public List<ChatRoomListResponseDto> findChatRooms(Long memberId) {
        if (!(chatRoomListRepository.findByMemberId(memberId).isEmpty())) {
            List<ChatRoomList> chatRoomLists = chatRoomListRepository.findByMemberId(memberId);

            return chatRoomLists.stream()
                    .map(chatRoomList -> new ChatRoomListResponseDto(
                            chatRoomList.getChatRoom().getId(),
                            chatRoomList.getNickname(),
                            chatRoomList.getBookmark()
                    ))
                    .collect(Collectors.toList());
        }
        else throw new CustomException(CHAT_NOT_FOUND);
    }


}
