package org.example.progettosettimanale6.dto;

public record MessaggioRichiesta(String destinatario, String testo, String idTemporaneo) {

    public MessaggioRichiesta {
        if (destinatario == null || destinatario.isBlank()) {
            throw new IllegalArgumentException("destinatario: non puo' essere vuoto");
        }
        if (testo == null || testo.isBlank()) {
            throw new IllegalArgumentException("testo: non puo' essere vuoto");
        }
        if (testo.length() > 2000) {
            throw new IllegalArgumentException("testo: al massimo 2000 caratteri");
        }
    }
}
