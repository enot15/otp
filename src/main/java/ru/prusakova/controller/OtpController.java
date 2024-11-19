package ru.prusakova.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.prusakova.dto.CheckOtpRequest;
import ru.prusakova.dto.GenerateOtpRequest;
import ru.prusakova.dto.common.CommonRequest;
import ru.prusakova.dto.common.CommonResponse;
import ru.prusakova.service.OtpService;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/otp")
public class OtpController {

    private final OtpService otpService;

    @PostMapping("/generateEndSend")
    public CommonResponse<?> generateEndSendOtp(@RequestBody @Valid CommonRequest<GenerateOtpRequest> request) {
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
