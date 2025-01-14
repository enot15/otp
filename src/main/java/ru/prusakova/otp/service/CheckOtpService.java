package ru.prusakova.otp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import ru.prusakova.otp.dto.CheckOtpRequest;
import ru.prusakova.otp.exception.OtpException;
import ru.prusakova.otp.model.CheckOtp;
import ru.prusakova.otp.model.SendOtp;
import ru.prusakova.otp.repository.CheckOtpRepository;
import ru.prusakova.otp.repository.SendOtpRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckOtpService {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final CheckOtpRepository checkOtpRepository;
    private final SendOtpRepository sendOtpRepository;

    public void checkOtp(CheckOtpRequest request) {
        SendOtp otp = sendOtpRepository.findFirstByProcessIdOrderByCreateTimeDesc(request.getProcessId().toString())
                .orElseThrow(() -> new OtpException("Не удалось найти информацию об отправке данного ОТП"));

        // create_time + ttl > текущее время
        if (otp.getCreateTime().plusSeconds(otp.getTtl()).isBefore(LocalDateTime.now())) {
            throw new OtpException("Время жизни ОТП истекло");
        }

        List<CheckOtp> checkOtpList = checkOtpRepository.findByProcessIdAndOtpAndCorrectIsTrue(request.getProcessId().toString(), request.getOtp());
        if (!checkOtpList.isEmpty()) {
            saveCheckOtpInDb(request, false);
            throw new OtpException("Попытка подтверждения ранее подтвержденного пароля");
        }

        // проверить корректность введенного пароля
        boolean matches = bCryptPasswordEncoder.matches(request.getProcessId() + request.getOtp(), otp.getSalt());
        if (matches) {
            log.info("Введен корректный пароль. processId={}", request.getProcessId());
            saveCheckOtpInDb(request, true);
        } else {
            saveCheckOtpInDb(request, false);
            throw new OtpException("Введен неверный ОТП");
        }
    }

    private void saveCheckOtpInDb(CheckOtpRequest request, boolean correct) {
        CheckOtp checkOtp = CheckOtp.builder()
                .processId(request.getProcessId().toString())
                .otp(request.getOtp())
                .checkTime(LocalDateTime.now())
                .correct(correct)
                .build();
        checkOtpRepository.save(checkOtp);
    }
}
