package ru.prusakova.otp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import ru.prusakova.otp.dto.GenerateOtpRequest;
import ru.prusakova.otp.dto.IntegrationStatus;
import ru.prusakova.otp.dto.KafkaSendOtpOutResponse;
import ru.prusakova.otp.dto.SendOtpStatus;
import ru.prusakova.otp.exception.OtpException;
import ru.prusakova.otp.model.SendOtp;
import ru.prusakova.otp.repository.SendOtpRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendOtpService {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final SendOtpRepository sendOtpRepository;
    private final TelegramSender telegramSender;

    public void generateOtp(GenerateOtpRequest request) {
        checkTimeOtp(request);

        // сгенерировать пароль
        String code = RandomStringUtils.randomNumeric(request.getLength());

        // сформировать salt
        String salt = bCryptPasswordEncoder.encode(request.getProcessId() + code);

        // сформировать message
        String messageFormat = String.format(request.getMessage(), code);

        //сохранить в бд
        String sendMessageKey = UUID.randomUUID().toString();
        SendOtp sendOtp = saveSendOtpInDb(request, salt, messageFormat, sendMessageKey);

        KafkaSendOtpOutResponse kafkaOutResponse = telegramSender.getKafkaOutResponse(sendOtp, sendMessageKey, request, messageFormat);
        if (kafkaOutResponse.getStatus() == IntegrationStatus.ERROR) {
            updateSendOtpInDb(sendOtp, SendOtpStatus.ERROR);
            throw new OtpException(kafkaOutResponse.getErrorMessage());
        }
        if (kafkaOutResponse.getStatus() == IntegrationStatus.SUCCESS) {
            log.info("Получен успешный ответ из кафки. id={}", kafkaOutResponse.getId());
            updateSendOtpInDb(sendOtp, SendOtpStatus.DELIVERED);
        }
    }

    private void checkTimeOtp(GenerateOtpRequest request) {
        sendOtpRepository.findFirstByProcessIdOrderByCreateTimeDesc(request.getProcessId().toString())
                .ifPresent(o -> {
                    // create_time + request.ttl < текущее время
                    if (o.getCreateTime().plusSeconds(request.getTtl()).isBefore(LocalDateTime.now())) {
                        throw new OtpException("Превышено время жизни ОТП");
                    }
                    // текущее время - create_time < request.resendTimeout
                    if (Duration.between(o.getCreateTime(), LocalDateTime.now()).toSeconds() < request.getResendTimeout()) {
                        throw new OtpException("Превышена частота попыток отправки ОТП");
                    }
                });

        List<SendOtp> otpList = sendOtpRepository.findByProcessId(request.getProcessId().toString());
        if (!otpList.isEmpty()) {
            // кол-во записей > resend_attempts с самым ранним create_time
            otpList.stream()
                    .min(Comparator.comparing(SendOtp::getCreateTime))
                    .ifPresent(otp -> {
                        if (otpList.size() > otp.getResendAttempts()) {
                            throw new OtpException("Превышено количество отправок ОТП");
                        }
                    });
        }
    }

    private SendOtp saveSendOtpInDb(GenerateOtpRequest request, String salt, String messageFormat, String sendMessageKey) {
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
                .status(SendOtpStatus.IN_PROGRESS)
                .sendTime(LocalDateTime.now())
                .build();
        return sendOtpRepository.save(sendOtp);
    }

    private void updateSendOtpInDb(SendOtp sendOtp, SendOtpStatus status) {
        sendOtp.setStatus(status);
        sendOtpRepository.save(sendOtp);
    }
}
