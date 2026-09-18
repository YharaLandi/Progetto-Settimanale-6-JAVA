package org.example.progettosettimanale6.dto;

import java.util.List;
import java.util.UUID;

public record PaginaMessaggi(UUID idConversazione, List<MessaggioRisposta> messaggi, long nonLetti) {
}
