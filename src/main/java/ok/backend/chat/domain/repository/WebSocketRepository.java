package ok.backend.chat.domain.repository;

import ok.backend.chat.domain.entity.WebSocket;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface WebSocketRepository extends MongoRepository<WebSocket, Long> {
    List<WebSocket> findBySessionIdOrderByLastConnectDesc(String sessionId);
}
