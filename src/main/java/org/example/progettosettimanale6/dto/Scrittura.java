package org.example.progettosettimanale6.dto;

public record Scrittura(String destinatario) {

    public Scrittura {
        if (destinatario == null || destinatario.isBlank()) {
            throw new IllegalArgumentException("destinatario: non puo' essere vuoto");
        }
    }
}
