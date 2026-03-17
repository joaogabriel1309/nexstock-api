package br.com.nexstock.nexstock_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class SyncException extends RuntimeException {

    public SyncException(String mensagem) {
        super(mensagem);
    }

    public SyncException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
