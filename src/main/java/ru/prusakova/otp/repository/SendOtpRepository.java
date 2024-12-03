package ru.prusakova.otp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.prusakova.otp.model.SendOtp;

import java.util.UUID;

public interface SendOtpRepository extends JpaRepository<SendOtp, UUID> {
}