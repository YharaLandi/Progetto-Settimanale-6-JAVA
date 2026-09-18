package org.example.progettosettimanale6.web;

import org.example.progettosettimanale6.service.LimiteRaggiuntoException;
import org.example.progettosettimanale6.service.RichiestaEsternaNonValidaException;
import org.example.progettosettimanale6.service.ServizioNonDisponibileException;
import org.example.progettosettimanale6.service.UtenteNonAutenticatoException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(UtenteNonAutenticatoException.class)
    public ResponseEntity<ApiError> nonAutenticato(UtenteNonAutenticatoException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiError.di(401, "Unauthorized", ex.getMessage()));
    }

    @ExceptionHandler(ServizioNonDisponibileException.class)
    public ResponseEntity<ApiError> servizioGiu(ServizioNonDisponibileException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ApiError.di(502, "Bad Gateway",
                        "il servizio di generazione non e' raggiungibile in questo momento, riprova"));
    }

    @ExceptionHandler(RichiestaEsternaNonValidaException.class)
    public ResponseEntity<ApiError> richiestaEsterna(RichiestaEsternaNonValidaException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ApiError.di(502, "Bad Gateway",
                        "errore di configurazione del servizio di generazione"));
    }

    @ExceptionHandler(LimiteRaggiuntoException.class)
    public ResponseEntity<ApiError> limite(LimiteRaggiuntoException ex) {
        var intestazioni = new HttpHeaders();
        if (ex.riprovaDopo() != null) {
            intestazioni.add("Retry-After", ex.riprovaDopo());
        }
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .headers(intestazioni)
                .body(ApiError.di(429, "Too Many Requests", "troppe richieste in questo momento"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleUnreadable(HttpMessageNotReadableException ex) {
        Throwable cause = ex;
        while (cause != null) {
            if (cause instanceof IllegalArgumentException) {
                return ApiError.badRequest(List.of(cause.getMessage()));
            }
            cause = cause.getCause();
        }
        return ApiError.badRequest(List.of("corpo della richiesta non leggibile"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleIllegalArgument(IllegalArgumentException ex) {
        return ApiError.badRequest(List.of(ex.getMessage()));
    }
}
