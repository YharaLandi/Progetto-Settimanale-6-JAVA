package org.example.progettosettimanale6.service;

public class UtenteNonAutenticatoException extends RuntimeException {
    public UtenteNonAutenticatoException() {
        super("token mancante o non valido");
    }
}
