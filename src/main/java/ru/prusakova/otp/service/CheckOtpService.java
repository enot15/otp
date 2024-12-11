package ru.prusakova.otp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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

@Service
@RequiredArgsConstructor
public class CheckOtpService {

    private final CheckOtpRepository checkOtpRepository;
    private final SendOtpRepository sendOtpRepository;

    public void checkOtp(CheckOtpRequest request) {
        Optional<SendOtp> otp = sendOtpRepository.findFirstByProcessIdOrderByCreateTimeDesc(request.getProcessId().toString());
        if (otp.isPresent()) {
            // create_time + ttl > текущее время
            if (otp.get().getCreateTime().plusSeconds(otp.get().getTtl()).isBefore(LocalDateTime.now())) {
                throw new OtpException("Время жизни ОТП истекло");
            }

            List<CheckOtp> checkOtpList = checkOtpRepository.findByProcessIdAndOtpAndCorrectIsTrue(request.getProcessId().toString(), request.getOtp());
            if (!checkOtpList.isEmpty()) {
                saveCheckOtpInDb(request, false);
                throw new OtpException("Попытка подтверждения ранее подтвержденного пароля");
            }

            // проверить корректность введенного пароля
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            boolean matches = passwordEncoder.matches(request.getProcessId() + request.getOtp(), otp.get().getSalt());
            if (matches) {
                saveCheckOtpInDb(request, true);
            } else {
                saveCheckOtpInDb(request, false);
                throw new OtpException("Введен неверный ОТП");
            }
        } else {
            throw new OtpException("Не удалось найти информацию об отправке данного ОТП");
        }
    }

    private void saveCheckOtpInDb(CheckOtpRequest request, boolean correct) {
        CheckOtp checkOtp = CheckOtp.builder()
                .processId(request.getProcessId().toString())
                .otp(request.getOtp())
                .checkTime(LocalDateTime.now())
                .correct(false)
                .build();
        checkOtpRepository.save(checkOtp);
    }
}
