package ru.prusakova.otp.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import ru.prusakova.otp.dto.KafkaSendOtpOutResponse;
import ru.prusakova.otp.util.JsonUtil;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "otp.kafka.send-otp-out", name = "enabled", havingValue = "true")
public class SendOtpConsumer {

    private final JsonUtil jsonUtil;

    @KafkaListener(topics = "${otp.kafka.send-otp-out.topic}")
    public void consume(ConsumerRecord<String, String> consumerRecord,
                        @Payload(required = false) String payload) {
        log.info("Ответ из кафка получен: {}", consumerRecord.toString());
        KafkaSendOtpOutResponse response = jsonUtil.fromJson(payload, KafkaSendOtpOutResponse.class);
    }
}
