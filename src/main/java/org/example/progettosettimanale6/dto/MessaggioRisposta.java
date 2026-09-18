package org.example.progettosettimanale6.dto;

import org.example.progettosettimanale6.model.Messaggio;

import java.time.Instant;
import java.util.UUID;

public record MessaggioRisposta(
        UUID id,
        UUID idConversazione,
        String mittente,
        String destinatario,
        String testo,
        Instant istante,
        Instant deliveredAt,
        String stato,
        String idTemporaneo) {

    public static MessaggioRisposta da(Messaggio m) {
        return con(m, null);
    }

    public static MessaggioRisposta con(Messaggio m, String mittenteUsername,
                                        String destinatarioUsername, String idTemporaneo) {
        return new MessaggioRisposta(
                m.getId(),
                m.getConversazione().getId(),
                mittenteUsername,
                destinatarioUsername,
                m.getTesto(),
                m.getIstante(),
                m.getDeliveredAt(),
                m.getStato().name(),
                idTemporaneo);
    }

    // Usato solo dentro transazioni dove le associazioni lazy sono già inizializzate
    public static MessaggioRisposta con(Messaggio m, String idTemporaneo) {
        return new MessaggioRisposta(
                m.getId(),
                m.getConversazione().getId(),
                m.getMittente().getUsername(),
                m.getDestinatario().getUsername(),
                m.getTesto(),
                m.getIstante(),
                m.getDeliveredAt(),
                m.getStato().name(),
                idTemporaneo);
    }
}
