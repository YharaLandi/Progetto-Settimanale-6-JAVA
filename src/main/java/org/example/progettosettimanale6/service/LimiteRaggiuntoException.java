package org.example.progettosettimanale6.service;

public class LimiteRaggiuntoException extends RuntimeException {

    private final String riprovaDopo;

    public LimiteRaggiuntoException(String riprovaDopo) {
        super("limite di frequenza raggiunto");
        this.riprovaDopo = riprovaDopo;
    }

    public String riprovaDopo() {
        return riprovaDopo;
    }
}
