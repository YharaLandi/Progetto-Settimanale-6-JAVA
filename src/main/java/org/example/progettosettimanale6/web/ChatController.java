package org.example.progettosettimanale6.web;

import org.example.progettosettimanale6.dto.MessaggioRichiesta;
import org.example.progettosettimanale6.dto.Scrittura;
import org.example.progettosettimanale6.dto.SegnaLetti;
import org.example.progettosettimanale6.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final ChatService servizio;

    public ChatController(ChatService servizio) {
        this.servizio = servizio;
    }

    @MessageMapping("/invia")
    public void ricevi(MessaggioRichiesta richiesta, Principal mittente) {
        if (mittente == null) {
            log.warn("messaggio da una sessione anonima: scartato");
            return;
        }
        servizio.inoltra(mittente.getName(), richiesta.destinatario(),
                richiesta.testo(), richiesta.idTemporaneo());
    }

    @MessageMapping("/letti")
    public void letti(SegnaLetti richiesta, Principal utente) {
        if (utente == null) return;
        servizio.segnaLetti(richiesta.idConversazione(), utente.getName());
    }

    @MessageMapping("/scrive")
    public void scrive(Scrittura richiesta, Principal utente) {
        if (utente == null) return;
        servizio.scrive(utente.getName(), richiesta.destinatario());
    }
}
