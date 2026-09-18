package org.example.progettosettimanale6.dto;

import java.util.UUID;

public record SuggerimentoRichiesta(UUID idConversazione) {

    public SuggerimentoRichiesta {
        if (idConversazione == null) {
            throw new IllegalArgumentException("idConversazione: obbligatorio");
        }
    }
}
