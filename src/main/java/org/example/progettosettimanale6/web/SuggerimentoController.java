package org.example.progettosettimanale6.web;

import org.example.progettosettimanale6.dto.SuggerimentoRichiesta;
import org.example.progettosettimanale6.dto.SuggerimentoRisposta;
import org.example.progettosettimanale6.service.LlmService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api")
public class SuggerimentoController {

    private final LlmService llmService;

    public SuggerimentoController(LlmService llmService) {
        this.llmService = llmService;
    }

    @PostMapping("/suggerisci")
    public SuggerimentoRisposta suggerisci(@RequestBody SuggerimentoRichiesta richiesta,
                                            Principal principal) {
        String suggerimento = llmService.suggerisci(richiesta.idConversazione(), principal.getName());
        return new SuggerimentoRisposta(suggerimento);
    }
}
