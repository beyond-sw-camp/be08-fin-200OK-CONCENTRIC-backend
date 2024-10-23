package ok.backend.chat.domain.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;

class ChatMessageTest {
    @Test
    public void test() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        ChatMessage message = ChatMessage.builder()
                .chatRoomId(1L)
                .memberId(1L)
                .nickname("hj")
                .message("test message")
                .fileId(1L)
                .fileName("filename")
                .createAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")).toString())
                .build();

        // 직렬화 테스트
        String jsonMessage = objectMapper.writeValueAsString(message);
        System.out.println("Serialized ChatMessage: " + jsonMessage);

        // 역직렬화 테스트
        ChatMessage deserializedMessage = objectMapper.readValue(jsonMessage, ChatMessage.class);
        System.out.println("Deserialized ChatMessage: " + deserializedMessage);
    }
}