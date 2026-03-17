package br.com.nexstock.nexstock_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ContratoInativoException extends RuntimeException {

    public ContratoInativoException(String status) {
        super(String.format("Contrato não está ativo. Status atual: %s", status));
    }
}
