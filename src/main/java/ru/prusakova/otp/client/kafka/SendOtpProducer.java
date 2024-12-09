package ru.prusakova.otp.client.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.prusakova.otp.dto.KafkaSendOtpInRequest;
import ru.prusakova.otp.util.JsonUtil;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "otp.kafka.send-otp", name = "enabled", havingValue = "true")
public class SendOtpProducer {

    private final JsonUtil jsonUtil;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${otp.kafka.send-otp.topic-in}")
    private String topicIn;

    public void sendMessage(String sendMessageKey, String telegramChatId, String message) {
        KafkaSendOtpInRequest request = KafkaSendOtpInRequest.builder()
                .id(sendMessageKey)
                .telegramChatId(telegramChatId)
                .message(message)
                .build();

        kafkaTemplate.send(topicIn, jsonUtil.toJson(request))
                .thenAccept(sendResult -> log.info("Запрос отправлен в кафку: {}", sendResult));;
    }
}
