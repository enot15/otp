package ru.prusakova.otp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
public class KafkaSendOtpOutResponse {

    private String id;
    private Status status;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String errorMessage;
}
