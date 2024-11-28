package ru.prusakova.otp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.prusakova.otp.model.CheckOtp;

import java.util.UUID;

public interface CheckOtpRepository extends JpaRepository<CheckOtp, UUID> {
}