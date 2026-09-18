package org.example.progettosettimanale6.dto;

import java.util.UUID;

public record SegnaLetti(UUID idConversazione) {

    public SegnaLetti {
        if (idConversazione == null) {
            throw new IllegalArgumentException("idConversazione: obbligatorio");
        }
    }
}
