package ru.prusakova.otp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.prusakova.otp.dto.CheckOtpRequest;
import ru.prusakova.otp.dto.GenerateOtpRequest;
import ru.prusakova.otp.dto.common.CommonRequest;
import ru.prusakova.otp.dto.common.CommonResponse;
import ru.prusakova.otp.service.OtpService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/otp")
public class OtpController {

    private final OtpService otpService;

    @PostMapping("/generateAndSend")
    public CommonResponse<?> generateAndSendOtp(@RequestBody @Valid CommonRequest<GenerateOtpRequest> request) {
        otpService.generateOtp(request.getBody());

        return CommonResponse.builder()
                .build();
    }

    @PostMapping("/check")
    public CommonResponse<?> checkOtp(@RequestBody @Valid CommonRequest<CheckOtpRequest> request) {
        otpService.checkOtp(request.getBody());

        return CommonResponse.builder()
                .build();
    }
}
