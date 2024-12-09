package ru.prusakova.otp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KafkaSendOtpInRequest {

    private String id;
    private String telegramChatId;
    private String message;
}
