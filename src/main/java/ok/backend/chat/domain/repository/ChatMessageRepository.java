package ok.backend.chat.domain.repository;

import ok.backend.chat.domain.entity.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, Long>{
    List<ChatMessage> findByChatRoomId(Long chatRoomId);
}
