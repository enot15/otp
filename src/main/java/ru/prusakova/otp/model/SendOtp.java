package ru.prusakova.otp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.prusakova.otp.dto.SendOtpStatus;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class SendOtp extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String processId;
    private String telegramChatId;
    private String message;
    private Integer length;
    private Integer ttl;
    private Integer resendAttempts;
    private Integer resendTimeout;
    private String salt;
    private String sendMessageKey;
    @Enumerated(value = EnumType.STRING)
    private SendOtpStatus status;
    private LocalDateTime sendTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SendOtp sendOtp = (SendOtp) o;
        return Objects.equals(id, sendOtp.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
