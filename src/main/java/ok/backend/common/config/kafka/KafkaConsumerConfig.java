package ok.backend.common.config.kafka;

import ok.backend.chat.domain.entity.ChatMessage;
import org.apache.kafka.common.serialization.StringDeserializer;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@Slf4j
@EnableKafka
@Configuration
public class KafkaConsumerConfig {
    // ConsumerFactory를 지정하여 Kafka 컨슈머의 설정을 제공

    @Value("${spring.kafka.bootstrap-servers}")
    private String broker;

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ChatMessage> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ChatMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, ChatMessage> consumerFactory() {
        JsonDeserializer<ChatMessage> deserializer = new JsonDeserializer<>(ChatMessage.class);
        deserializer.setRemoveTypeHeaders(false);
        deserializer.addTrustedPackages("*"); // 패키지 신뢰 목록에 모든 패키지를 추가
        deserializer.setUseTypeMapperForKey(true); // 메시지 키에 타입 정보 추가

        ImmutableMap<String, Object> config = ImmutableMap.<String, Object>builder()
//                .put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConstants.BROKER)
                .put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, broker)
                .put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class)
                .put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer)
                .put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest") // 가장 최신 메세지부터 읽음
                .put(ConsumerConfig.GROUP_ID_CONFIG, KafkaConstants.GROUP_ID)
                .build();

        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), deserializer);

    }

}
