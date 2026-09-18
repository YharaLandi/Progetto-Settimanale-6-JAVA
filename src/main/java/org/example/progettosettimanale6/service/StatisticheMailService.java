package org.example.progettosettimanale6.service;

import jakarta.mail.MessagingException;
import org.example.progettosettimanale6.dto.StatisticheRisposta;
import org.example.progettosettimanale6.model.Utente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDate;
import java.util.Map;

@Service
public class StatisticheMailService {

    private static final Logger log = LoggerFactory.getLogger(StatisticheMailService.class);

    private final SpringTemplateEngine motore;
    private final EmailService email;
    private final UtenteService utenteService;
    private final StatisticheService statisticheService;

    public StatisticheMailService(SpringTemplateEngine motore,
                                   EmailService email,
                                   UtenteService utenteService,
                                   StatisticheService statisticheService) {
        this.motore = motore;
        this.email = email;
        this.utenteService = utenteService;
        this.statisticheService = statisticheService;
    }

    public void invia(String username) {
        Utente utente = utenteService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("utente non trovato: " + username));

        StatisticheRisposta stats = statisticheService.statistiche(username);

        var ctx = new Context();
        ctx.setVariables(Map.of(
                "nome", utente.getNomeCompleto(),
                "stats", stats,
                "data", LocalDate.now().toString()));

        String html = motore.process("email/statistiche", ctx);
        String testo = "Ciao " + utente.getNomeCompleto() + ", ecco le tue statistiche ChatAI.";

        try {
            email.inviaHtml(utente.getEmail(), "Le tue statistiche ChatAI", testo, html);
        } catch (MessagingException ex) {
            log.error("invio email statistiche fallito per {}: {}", username, ex.getMessage());
            throw new RuntimeException("invio email fallito", ex);
        }
    }
}
