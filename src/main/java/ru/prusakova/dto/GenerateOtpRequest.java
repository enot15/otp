package ru.prusakova.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Min(4)
    @Max(8)
    @NotNull(message = "Длина одноразового пароля не может отсутствовать или быть null")
    private Integer length;

    @Min(30)
    @NotNull(message = "Время жизни одноразового пароля в секундах не может отсутствовать или быть null")
    private Integer ttl;

    @Min(1)
    @Max(3)
    @NotNull(message = "Количество возможных повторных отправок кода не может отсутствовать или быть null")
    private Integer resendAttempts;

    @Min(30)
    @NotNull(message = "Таймаут перед повторным запросом пароля в секундах не может отсутствовать или быть null")
    private Integer resendTimeout;
}
