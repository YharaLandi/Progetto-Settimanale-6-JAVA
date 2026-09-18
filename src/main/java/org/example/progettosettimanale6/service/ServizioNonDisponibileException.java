package org.example.progettosettimanale6.service;

public class ServizioNonDisponibileException extends RuntimeException {

    public ServizioNonDisponibileException(String messaggio) {
        super(messaggio);
    }
}
