package br.com.nexstock.nexstock_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class LimiteDispositivosException extends RuntimeException {

    public LimiteDispositivosException(int limite, String plano) {
        super(String.format(
            "Limite de %d dispositivo(s) atingido para o plano '%s'. Faça upgrade do plano para adicionar mais.",
            limite, plano
        ));
    }
}
