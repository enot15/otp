package ru.prusakova.otp.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.prusakova.otp.client.kafka.SendOtpProducer;
import ru.prusakova.otp.dto.GenerateOtpRequest;
import ru.prusakova.otp.dto.KafkaSendOtpOutResponse;
import ru.prusakova.otp.dto.StatusDeliveryEnum;
import ru.prusakova.otp.dto.StatusEnum;
import ru.prusakova.otp.exception.OtpException;
import ru.prusakova.otp.listener.KafkaMessageContext;
import ru.prusakova.otp.model.SendOtp;
import ru.prusakova.otp.repository.SendOtpRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
public class SendOtpService {

    private final SendOtpRepository sendOtpRepository;
    private final SendOtpProducer sendOtpProducer;
    private final KafkaMessageContext kafkaMessageContext;

    public void generateOtp(GenerateOtpRequest request) {
        Optional<SendOtp> otp = sendOtpRepository.findFirstByProcessIdOrderByCreateTimeDesc(request.getProcessId().toString());
        if (otp.isPresent()) {
            // create_time + request.ttl < текущее время
            if (otp.get().getCreateTime().plusSeconds(request.getTtl()).isBefore(LocalDateTime.now())) {
                throw new OtpException("Превышено время жизни ОТП");
            }

            // текущее время - create_time < request.resendTimeout
            if (Duration.between(otp.get().getCreateTime(), LocalDateTime.now()).toSeconds() < request.getResendTimeout()) {
                throw new OtpException("Превышена частота попыток отправки ОТП");
            }
        }
        List<SendOtp> otpList = sendOtpRepository.findByProcessId(request.getProcessId().toString());
        if (!otpList.isEmpty()) {
            // кол-во записей > resend_attempts с самым ранним create_time
            otpList = otpList.stream()
                    .sorted(Comparator.comparing(SendOtp::getCreateTime).reversed())
                    .toList();
            if (otpList.size() > otpList.get(0).getResendAttempts()) {
                throw new OtpException("Превышено количество отправок ОТП");
            }
        }

        // сгенерировать пароль
        String code = RandomStringUtils.randomNumeric(request.getLength());

        // сформировать salt
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String salt = encoder.encode(request.getProcessId() + code);

        // сформировать message
        String messageFormat = String.format(request.getMessage(), code);

        //сохранить в бд
        String sendMessageKey = UUID.randomUUID().toString();
        saveSendOtpInDb(request, salt, messageFormat, sendMessageKey);

        // отправить запрос в топик IN
        sendOtpProducer.sendMessage(sendMessageKey, request.getTelegramChatId(), messageFormat);

        KafkaSendOtpOutResponse kafkaOutResponse = getKafkaOutResponse(sendMessageKey);
        if (kafkaOutResponse.getStatus() == StatusEnum.ERROR) {
            updateSendOtpInDb(sendMessageKey, StatusDeliveryEnum.ERROR);
            throw new OtpException(kafkaOutResponse.getErrorMessage());
        }
        if (kafkaOutResponse.getStatus() == StatusEnum.SUCCESS) {
            updateSendOtpInDb(sendMessageKey, StatusDeliveryEnum.DELIVERED);
        }
    }

    private KafkaSendOtpOutResponse getKafkaOutResponse(String sendMessageKey) {
        CompletableFuture<KafkaSendOtpOutResponse> responseCompletableFuture = kafkaMessageContext.createMessageCompletableFuture(sendMessageKey);
        try {
            return responseCompletableFuture
                    .get(50000, TimeUnit.MILLISECONDS);
        } catch (TimeoutException | ExecutionException | InterruptedException e) {
            updateSendOtpInDb(sendMessageKey, StatusDeliveryEnum.ERROR);
            throw new OtpException("Таймаут ожидания ответа от сервиса отправки сообщения", e);
        } finally {
            kafkaMessageContext.removeById(sendMessageKey);
        }
    }

    private void updateSendOtpInDb(String sendMessageKey, StatusDeliveryEnum status) {
        SendOtp otpBySendMessageKey = sendOtpRepository.findBySendMessageKey(sendMessageKey)
                .orElseThrow(() -> new OtpException("Не найдена сущность по sendMessageKey " + sendMessageKey));
        otpBySendMessageKey.setStatus(status);
        sendOtpRepository.save(otpBySendMessageKey);
    }

    private void saveSendOtpInDb(GenerateOtpRequest request, String salt, String messageFormat, String sendMessageKey) {
        SendOtp sendOtp = SendOtp.builder()
                .processId(request.getProcessId().toString())
                .telegramChatId(request.getTelegramChatId())
                .message(messageFormat)
                .length(request.getLength())
                .ttl(request.getTtl())
                .resendAttempts(request.getResendAttempts())
                .resendTimeout(request.getResendTimeout())
                .salt(salt)
                .sendMessageKey(sendMessageKey)
                .status(StatusDeliveryEnum.IN_PROGRESS)
                .sendTime(LocalDateTime.now())
                .build();
        sendOtpRepository.save(sendOtp);
    }
}
