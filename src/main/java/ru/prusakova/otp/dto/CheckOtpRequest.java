package ru.prusakova.otp.dto;

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
public class CheckOtpRequest {

    @NotNull(message = "Идентификатор процесса не может отсутствовать или быть null")
    private UUID processId;

    @NotNull(message = "Одноразовый пароль не может отсутствовать или быть null")
    private String otp;
}
