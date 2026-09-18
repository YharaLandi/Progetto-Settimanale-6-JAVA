package org.example.progettosettimanale6.web;

import jakarta.servlet.http.HttpServletRequest;
import org.example.progettosettimanale6.dto.StatisticheRisposta;
import org.example.progettosettimanale6.service.StatisticheMailService;
import org.example.progettosettimanale6.service.StatisticheService;
import org.example.progettosettimanale6.service.TokenStore;
import org.example.progettosettimanale6.service.UtenteNonAutenticatoException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/statistiche")
public class StatisticheController {

    private final StatisticheService statisticheService;
    private final StatisticheMailService statisticheMailService;
    private final TokenStore tokenStore;

    public StatisticheController(StatisticheService statisticheService,
                                  StatisticheMailService statisticheMailService,
                                  TokenStore tokenStore) {
        this.statisticheService = statisticheService;
        this.statisticheMailService = statisticheMailService;
        this.tokenStore = tokenStore;
    }

    @GetMapping
    public StatisticheRisposta statistiche(HttpServletRequest req) {
        String utente = utenteCorrente(req);
        return statisticheService.statistiche(utente);
    }

    @PostMapping("/email")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void inviaEmail(HttpServletRequest req) {
        String utente = utenteCorrente(req);
        statisticheMailService.invia(utente);
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
