package org.example.progettosettimanale6.dto;

import java.time.Instant;
import java.util.UUID;

public record ConversazioneRiepilogo(
        UUID idConversazione,
        String controparte,
        long nonLetti,
        String ultimoTesto,
        Instant ultimoIstante,
        boolean online) {
}
