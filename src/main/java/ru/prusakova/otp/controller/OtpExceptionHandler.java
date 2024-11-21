package ru.prusakova.otp.controller;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.prusakova.otp.dto.common.CommonResponse;
import ru.prusakova.otp.dto.common.ValidationError;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class OtpExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public CommonResponse<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        List<ValidationError> validationErrors = bindingResult.getFieldErrors().stream()
                .map(fieldError -> ValidationError.builder()
                        .field(fieldError.getField())
                        .message(fieldError.getDefaultMessage())
                        .build())
                .toList();

        log.warn("Ошибка валидации: {}", validationErrors, e);

        return CommonResponse.builder()
                .errorMessage("Ошибка валидации")
                .validationErrors(validationErrors)
                .build();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public CommonResponse<?> handleInvalidFormatException(HttpMessageNotReadableException e) {
        if (e.getCause() instanceof InvalidFormatException ife) {
            String path = ife.getPath().stream()
                    .map(JsonMappingException.Reference::getFieldName)
                    .collect(Collectors.joining("."));

            String errorMessage = "Ошибка валидации, некорректный формат поля '" + path + "'";
            log.error(errorMessage, e);

            return CommonResponse.builder()
                    .errorMessage(errorMessage)
                    .build();
        }

        return handleException(e);
    }

    @ExceptionHandler(Exception.class)
    public CommonResponse<?> handleException(Exception e) {
        log.error("Непредвиденное исключение: {} ", e.getMessage(), e);

        return CommonResponse.builder()
                .errorMessage("Непредвиденное исключение: " + e.getMessage())
                .build();
    }
}
