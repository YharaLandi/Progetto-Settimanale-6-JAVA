package org.example.progettosettimanale6.web;

import jakarta.servlet.http.HttpServletRequest;
import org.example.progettosettimanale6.dto.ConversazioneRiepilogo;
import org.example.progettosettimanale6.dto.NuovaChatRequest;
import org.example.progettosettimanale6.dto.PaginaMessaggi;
import org.example.progettosettimanale6.model.Conversazione;
import org.example.progettosettimanale6.model.Utente;
import org.example.progettosettimanale6.service.ChatService;
import org.example.progettosettimanale6.service.TokenStore;
import org.example.progettosettimanale6.service.UtenteNonAutenticatoException;
import org.example.progettosettimanale6.service.UtenteService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
public class ConversazioneController {

    private final ChatService chatService;
    private final UtenteService utenteService;
    private final TokenStore tokenStore;

    public ConversazioneController(ChatService chatService,
                                    UtenteService utenteService,
                                    TokenStore tokenStore) {
        this.chatService = chatService;
        this.utenteService = utenteService;
        this.tokenStore = tokenStore;
    }

    @PostMapping("/nuova")
    @ResponseStatus(HttpStatus.CREATED)
    public ConversazioneRiepilogo nuova(@RequestBody NuovaChatRequest richiesta,
                                         HttpServletRequest req) {
        String utenteUsername = utenteCorrente(req);
        Utente utente = utenteService.findByUsername(utenteUsername)
                .orElseThrow(() -> new IllegalArgumentException("utente non trovato"));
        Utente destinatario = utenteService.findByUsername(richiesta.usernameDestinatario())
                .orElseThrow(() -> new IllegalArgumentException("destinatario non trovato: "
                        + richiesta.usernameDestinatario()));
        Conversazione conv = chatService.trovaOCrea(utente, destinatario);
        return new ConversazioneRiepilogo(conv.getId(),
                destinatario.getUsername(), 0, null, null, false);
    }

    @GetMapping("/conversazioni")
    public List<ConversazioneRiepilogo> elenco(HttpServletRequest req) {
        String utente = utenteCorrente(req);
        return chatService.riepilogo(utente);
    }

    @GetMapping("/cronologia")
    public PaginaMessaggi cronologia(@RequestParam String conChi, HttpServletRequest req) {
        String utente = utenteCorrente(req);
        return chatService.cronologia(utente, conChi);
    }

    @PatchMapping("/{idConversazione}/consegnato")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void segnaConsegnato(@PathVariable UUID idConversazione, HttpServletRequest req) {
        String utente = utenteCorrente(req);
        chatService.segnaConsegnati(idConversazione, utente);
    }

    private String utenteCorrente(HttpServletRequest req) {
        String h = req.getHeader("Authorization");
        if (h != null && h.startsWith("Bearer ")) {
            return tokenStore.utenteDi(h.substring(7))
                    .orElseThrow(UtenteNonAutenticatoException::new);
        }
        throw new UtenteNonAutenticatoException();
    }
}
