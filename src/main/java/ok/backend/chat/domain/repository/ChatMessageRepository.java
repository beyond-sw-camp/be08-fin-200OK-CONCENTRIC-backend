package ok.backend.chat.domain.repository;

import ok.backend.chat.domain.entity.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, Long>{
}
