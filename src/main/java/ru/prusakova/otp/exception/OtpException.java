package ru.prusakova.otp.exception;

public class OtpException extends RuntimeException {

    public OtpException(String msg) {
        super(msg);
    }

    public OtpException(String msg, Exception e) {
        super(msg, e);
    }
}
