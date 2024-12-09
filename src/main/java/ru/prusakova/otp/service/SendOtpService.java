package ru.prusakova.otp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.prusakova.otp.client.kafka.SendOtpProducer;
import ru.prusakova.otp.dto.GenerateOtpRequest;
import ru.prusakova.otp.repository.SendOtpRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SendOtpService {

    private final SendOtpRepository sendOtpRepository;
    private final SendOtpProducer sendOtpProducer;

    public void generateOtp(GenerateOtpRequest request) {
        String sendMessageKey = UUID.randomUUID().toString();
        sendOtpProducer.sendMessage(sendMessageKey, request.getTelegramChatId(), request.getMessage());
    }
}
