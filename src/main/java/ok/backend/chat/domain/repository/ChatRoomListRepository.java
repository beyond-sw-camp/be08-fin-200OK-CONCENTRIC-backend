package ok.backend.chat.domain.repository;

import ok.backend.chat.domain.entity.ChatRoomList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomListRepository extends JpaRepository<ChatRoomList, Long> {

    @Query("SELECT c FROM ChatRoomList c WHERE c.chatRoom.id = :chatRoomId")
    List<ChatRoomList> findByChatRoomId(@Param("chatRoomId") Long chatRoomId);

    @Query("SELECT c FROM ChatRoomList c WHERE c.member.id = :memberId AND c.chatRoom.id = :chatRoomId")
    Optional<ChatRoomList> findByMemberIdAndChatRoomIdOne(@Param("memberId") Long memberId, @Param("chatRoomId") Long chatRoomId);

    @Query("SELECT c FROM ChatRoomList c WHERE c.member.id = :memberId")
    List<ChatRoomList> findByMemberId(@Param("memberId") Long memberId);

}
