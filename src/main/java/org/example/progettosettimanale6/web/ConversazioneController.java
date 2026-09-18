package org.example.progettosettimanale6.web;

import org.example.progettosettimanale6.dto.ConversazioneRiepilogo;
import org.example.progettosettimanale6.dto.NuovaChatRequest;
import org.example.progettosettimanale6.dto.PaginaMessaggi;
import org.example.progettosettimanale6.model.Conversazione;
import org.example.progettosettimanale6.model.Utente;
import org.example.progettosettimanale6.service.ChatService;
import org.example.progettosettimanale6.service.UtenteService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
public class ConversazioneController {

    private final ChatService chatService;
    private final UtenteService utenteService;

    public ConversazioneController(ChatService chatService, UtenteService utenteService) {
        this.chatService = chatService;
        this.utenteService = utenteService;
    }

    @PostMapping("/nuova")
    @ResponseStatus(HttpStatus.CREATED)
    public ConversazioneRiepilogo nuova(@RequestBody NuovaChatRequest richiesta, Principal principal) {
        Utente utente = utenteService.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("utente non trovato"));
        Utente destinatario = utenteService.findByUsername(richiesta.usernameDestinatario())
                .orElseThrow(() -> new IllegalArgumentException("destinatario non trovato: "
                        + richiesta.usernameDestinatario()));
        Conversazione conv = chatService.trovaOCrea(utente, destinatario);
        return new ConversazioneRiepilogo(conv.getId(), destinatario.getUsername(), 0, null, null, false);
    }

    @GetMapping("/conversazioni")
    public List<ConversazioneRiepilogo> elenco(Principal principal) {
        return chatService.riepilogo(principal.getName());
    }

    @GetMapping("/cronologia")
    public PaginaMessaggi cronologia(@RequestParam String conChi, Principal principal) {
        return chatService.cronologia(principal.getName(), conChi);
    }

    @PatchMapping("/{idConversazione}/consegnato")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void segnaConsegnato(@PathVariable UUID idConversazione, Principal principal) {
        chatService.segnaConsegnati(idConversazione, principal.getName());
    }
}
