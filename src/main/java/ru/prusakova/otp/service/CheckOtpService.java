package ru.prusakova.otp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.prusakova.otp.dto.CheckOtpRequest;
import ru.prusakova.otp.repository.CheckOtpRepository;

@Service
@RequiredArgsConstructor
public class CheckOtpService {

    private final CheckOtpRepository checkOtpRepository;

    public void checkOtp(CheckOtpRequest request) {

    }
}
