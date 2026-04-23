package br.com.nexstock.nexstock_api.exception;

public class FalhaUploadException extends RuntimeException {

    public FalhaUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}
