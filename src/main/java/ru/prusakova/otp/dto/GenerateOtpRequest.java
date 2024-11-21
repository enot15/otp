package ru.prusakova.otp.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;

import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GenerateOtpRequest {

    @NotNull(message = "Идентификатор процесса не может отсутствовать или быть null")
    private UUID processId;

    @NotNull(message = "Идентификатор тереграм чата не может отсутствовать или быть null")
    private String telegramChatId;

    @NotNull(message = "Текст сообщения не может отсутствовать или быть null")
    private String message;

    @Range(min = 4, max = 8, message = "Длина одноразового пароля должна быть от 4 до 8")
    @NotNull(message = "Длина одноразового пароля не может отсутствовать или быть null")
    private Integer length;

    @Min(value = 30, message = "Время жизни одноразового пароля должно быть не менее 30 секунд")
    @NotNull(message = "Время жизни одноразового пароля в секундах не может отсутствовать или быть null")
    private Integer ttl;

    @Range(min = 1, max = 3, message = "Количество возможных повторных отправок кода должно быть от 1 до 3")
    @NotNull(message = "Количество возможных повторных отправок кода не может отсутствовать или быть null")
    private Integer resendAttempts;

    @Min(value = 30, message = "Таймаут перед повторным запросом пароля должен быть не менее 30 секунд")
    @NotNull(message = "Таймаут перед повторным запросом пароля в секундах не может отсутствовать или быть null")
    private Integer resendTimeout;
}
