package org.example.progettosettimanale6.web;

import jakarta.servlet.http.HttpServletRequest;
import org.example.progettosettimanale6.dto.SuggerimentoRichiesta;
import org.example.progettosettimanale6.dto.SuggerimentoRisposta;
import org.example.progettosettimanale6.service.LlmService;
import org.example.progettosettimanale6.service.TokenStore;
import org.example.progettosettimanale6.service.UtenteNonAutenticatoException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class SuggerimentoController {

    private final LlmService llmService;
    private final TokenStore tokenStore;

    public SuggerimentoController(LlmService llmService, TokenStore tokenStore) {
        this.llmService = llmService;
        this.tokenStore = tokenStore;
    }

    @PostMapping("/suggerisci")
    public SuggerimentoRisposta suggerisci(@RequestBody SuggerimentoRichiesta richiesta,
                                            HttpServletRequest req) {
        String utente = utenteCorrente(req);
        String suggerimento = llmService.suggerisci(richiesta.idConversazione(), utente);
        return new SuggerimentoRisposta(suggerimento);
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
