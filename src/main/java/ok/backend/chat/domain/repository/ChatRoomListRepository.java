package ok.backend.chat.domain.repository;

import ok.backend.chat.domain.entity.ChatRoomList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomListRepository extends JpaRepository<ChatRoomList, Long> {

    List<ChatRoomList> findByChatRoomId(Long chatRoomId);

    Optional<ChatRoomList> findByMemberIdAndChatRoomId(Long memberId, Long chatRoomId);

    List<ChatRoomList> findByMemberId(Long memberId);

    List<ChatRoomList> findByChatRoomIdAndMemberIdNot(Long chatRoomId, Long memberId);

    List<ChatRoomList> findByMemberIdAndChatRoomIsActiveTrue(Long chatRoomId);

}
