package ru.prusakova.otp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.prusakova.otp.client.kafka.SendOtpProducer;
import ru.prusakova.otp.dto.GenerateOtpRequest;
import ru.prusakova.otp.dto.KafkaSendOtpOutResponse;
import ru.prusakova.otp.dto.SendOtpStatus;
import ru.prusakova.otp.dto.Status;
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

    public void sendTelegram(String sendMessageKey, GenerateOtpRequest request, String messageFormat) {
        // отправить запрос в топик IN
        sendOtpProducer.sendMessage(sendMessageKey, request.getTelegramChatId(), messageFormat);

        KafkaSendOtpOutResponse kafkaOutResponse = getKafkaOutResponse(sendMessageKey);
        if (kafkaOutResponse.getStatus() == Status.ERROR) {
            updateSendOtpInDb(sendMessageKey, SendOtpStatus.ERROR);
            throw new OtpException(kafkaOutResponse.getErrorMessage());
        }
        if (kafkaOutResponse.getStatus() == Status.SUCCESS) {
            log.info("Получен успешный ответ из кафки. id={}", kafkaOutResponse.getId());
            updateSendOtpInDb(sendMessageKey, SendOtpStatus.DELIVERED);
        }
    }

    private KafkaSendOtpOutResponse getKafkaOutResponse(String sendMessageKey) {
        CompletableFuture<KafkaSendOtpOutResponse> responseCompletableFuture = kafkaMessageContext.createMessageCompletableFuture(sendMessageKey);
        try {
            return responseCompletableFuture
                    .get(5000, TimeUnit.MILLISECONDS);
        } catch (TimeoutException | ExecutionException | InterruptedException e) {
            updateSendOtpInDb(sendMessageKey, SendOtpStatus.ERROR);
            throw new OtpException("Таймаут ожидания ответа от сервиса отправки сообщения", e);
        } finally {
            kafkaMessageContext.removeById(sendMessageKey);
        }
    }

    private void updateSendOtpInDb(String sendMessageKey, SendOtpStatus status) {
        SendOtp otpBySendMessageKey = sendOtpRepository.findBySendMessageKey(sendMessageKey)
                .orElseThrow(() -> new OtpException("Не найдена сущность по sendMessageKey " + sendMessageKey));
        otpBySendMessageKey.setStatus(status);
        sendOtpRepository.save(otpBySendMessageKey);
    }
}
