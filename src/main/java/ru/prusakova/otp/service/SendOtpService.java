package ru.prusakova.otp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.prusakova.otp.dto.GenerateOtpRequest;
import ru.prusakova.otp.repository.SendOtpRepository;

@Service
@RequiredArgsConstructor
public class SendOtpService {

    private final SendOtpRepository sendOtpRepository;

    public void generateOtp(GenerateOtpRequest request) {

    }
}
