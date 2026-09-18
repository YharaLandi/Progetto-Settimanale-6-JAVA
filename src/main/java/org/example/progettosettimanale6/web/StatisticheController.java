package org.example.progettosettimanale6.web;

import org.example.progettosettimanale6.dto.StatisticheRisposta;
import org.example.progettosettimanale6.service.StatisticheMailService;
import org.example.progettosettimanale6.service.StatisticheService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/statistiche")
public class StatisticheController {

    private final StatisticheService statisticheService;
    private final StatisticheMailService statisticheMailService;

    public StatisticheController(StatisticheService statisticheService,
                                  StatisticheMailService statisticheMailService) {
        this.statisticheService = statisticheService;
        this.statisticheMailService = statisticheMailService;
    }

    @GetMapping
    public StatisticheRisposta statistiche(Principal principal) {
        return statisticheService.statistiche(principal.getName());
    }

    @PostMapping("/email")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void inviaEmail(Principal principal) {
        statisticheMailService.invia(principal.getName());
    }
}
