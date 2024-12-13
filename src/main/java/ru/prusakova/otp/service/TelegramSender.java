package ru.prusakova.otp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.prusakova.otp.client.kafka.SendOtpProducer;
import ru.prusakova.otp.dto.GenerateOtpRequest;
import ru.prusakova.otp.dto.KafkaSendOtpOutResponse;
import ru.prusakova.otp.dto.SendOtpStatus;
import ru.prusakova.otp.dto.IntegrationStatus;
import ru.prusakova.otp.exception.OtpException;
import ru.prusakova.otp.listener.KafkaMessageContext;
import ru.prusakova.otp.model.SendOtp;
import ru.prusakova.otp.repository.SendOtpRepository;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramSender {

    private final SendOtpRepository sendOtpRepository;
    private final SendOtpProducer sendOtpProducer;
    private final KafkaMessageContext kafkaMessageContext;

    public KafkaSendOtpOutResponse getKafkaOutResponse(SendOtp sendOtp, String sendMessageKey, GenerateOtpRequest request, String messageFormat) {
        // отправить запрос в топик IN
        sendOtpProducer.sendMessage(sendMessageKey, request.getTelegramChatId(), messageFormat);

        CompletableFuture<KafkaSendOtpOutResponse> responseCompletableFuture = kafkaMessageContext.createMessageCompletableFuture(sendMessageKey);
        try {
            return responseCompletableFuture
                    .get(5000, TimeUnit.MILLISECONDS);
        } catch (TimeoutException | ExecutionException | InterruptedException e) {
            sendOtp.setStatus(SendOtpStatus.ERROR);
            sendOtpRepository.save(sendOtp);
            throw new OtpException("Таймаут ожидания ответа от сервиса отправки сообщения", e);
        } finally {
            kafkaMessageContext.removeById(sendMessageKey);
        }
    }
}
