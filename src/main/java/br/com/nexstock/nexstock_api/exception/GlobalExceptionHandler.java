package br.com.nexstock.nexstock_api.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.*;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    public record ErroResponse(
        LocalDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        String path,
        Map<String, String> detalhes
    ) {}

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> handleNaoEncontrado(
            RecursoNaoEncontradoException ex, WebRequest request) {

        log.warn("Recurso não encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro(
            HttpStatus.NOT_FOUND, "Recurso não encontrado", ex.getMessage(), request, null
        ));
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ErroResponse> handleRegraDeNegocio(
            RegraDeNegocioException ex, WebRequest request) {

        log.warn("Regra de negócio violada: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(erro(
            HttpStatus.CONFLICT, "Conflito de negócio", ex.getMessage(), request, null
        ));
    }

    @ExceptionHandler(ContratoInativoException.class)
    public ResponseEntity<ErroResponse> handleContratoInativo(
            ContratoInativoException ex, WebRequest request) {

        log.warn("Acesso com contrato inativo: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(erro(
            HttpStatus.FORBIDDEN, "Contrato inativo", ex.getMessage(), request, null
        ));
    }

    @ExceptionHandler(LimiteDispositivosException.class)
    public ResponseEntity<ErroResponse> handleLimiteDispositivos(
            LimiteDispositivosException ex, WebRequest request) {

        log.warn("Limite de dispositivos atingido: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(erro(
            HttpStatus.UNPROCESSABLE_ENTITY, "Limite atingido", ex.getMessage(), request, null
        ));
    }

    @ExceptionHandler(SyncException.class)
    public ResponseEntity<ErroResponse> handleSync(
            SyncException ex, WebRequest request) {

        log.error("Erro de sincronização: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(erro(
            HttpStatus.UNPROCESSABLE_ENTITY, "Erro de sincronização", ex.getMessage(), request, null
        ));
    }

    @ExceptionHandler(ArquivoInvalidoException.class)
    public ResponseEntity<ErroResponse> handleArquivoInvalido(
            ArquivoInvalidoException ex, WebRequest request) {

        log.warn("Arquivo invalido: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro(
            HttpStatus.BAD_REQUEST, "Arquivo invalido", ex.getMessage(), request, null
        ));
    }

    @ExceptionHandler(FalhaUploadException.class)
    public ResponseEntity<ErroResponse> handleFalhaUpload(
            FalhaUploadException ex, WebRequest request) {

        log.error("Falha no upload: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(erro(
            HttpStatus.BAD_GATEWAY, "Falha no upload",
            "Nao foi possivel enviar o arquivo. Tente novamente.", request, null
        ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroResponse> handleAccessDenied(
            AccessDeniedException ex, WebRequest request) {

        log.warn("Acesso negado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(erro(
            HttpStatus.FORBIDDEN, "Acesso negado", ex.getMessage(), request, null
        ));
    }

    @Override
    protected ResponseEntity<Object> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        log.warn("Upload excedeu o tamanho maximo permitido");
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(erro(
            HttpStatus.PAYLOAD_TOO_LARGE, "Arquivo muito grande",
            "O arquivo enviado excede o tamanho maximo permitido.", request, null
        ));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Map<String, String> detalhes = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            detalhes.put(fe.getField(), fe.getDefaultMessage());
        }

        log.warn("Erro de validação: {}", detalhes);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro(
            HttpStatus.BAD_REQUEST, "Dados inválidos",
            "Um ou mais campos falharam na validação", request, detalhes
        ));
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> handleBadCredentials(BadCredentialsException ex) {
        return Map.of("erro", "Email ou senha inválidos");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> handleGenerico(Exception ex, WebRequest request) {
        log.error("Erro inesperado: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro(
            HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
            "Ocorreu um erro inesperado. Tente novamente.", request, null
        ));
    }

    private ErroResponse erro(HttpStatus status, String erro, String mensagem,
                               WebRequest request, Map<String, String> detalhes) {
        return new ErroResponse(
            LocalDateTime.now(),
            status.value(),
            erro,
            mensagem,
            request.getDescription(false).replace("uri=", ""),
            detalhes
        );
    }
}
