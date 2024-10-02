package ok.backend.chat.domain.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ChatMessageTest {
    @Test
    public void test() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        ChatMessage message = ChatMessage.builder()
                .chatRoomId(1L)
                .memberId(1L)
                .message("테스트 메시지")
                .fileUrl(null)
                .createAt(LocalDateTime.now().toString())
                .build();

        // 직렬화 테스트
        String jsonMessage = objectMapper.writeValueAsString(message);
        System.out.println("Serialized ChatMessage: " + jsonMessage);

        // 역직렬화 테스트
        ChatMessage deserializedMessage = objectMapper.readValue(jsonMessage, ChatMessage.class);
        System.out.println("Deserialized ChatMessage: " + deserializedMessage);
    }
}