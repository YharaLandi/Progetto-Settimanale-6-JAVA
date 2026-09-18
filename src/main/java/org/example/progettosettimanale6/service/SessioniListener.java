package org.example.progettosettimanale6.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class SessioniListener {

    private static final Logger log = LoggerFactory.getLogger(SessioniListener.class);

    private final AtomicInteger aperte = new AtomicInteger();

    @EventListener
    public void suConnessione(SessionConnectedEvent evento) {
        log.info("sessione aperta   utente={} aperte={}", nome(evento.getUser()), aperte.incrementAndGet());
    }

    @EventListener
    public void suDisconnessione(SessionDisconnectEvent evento) {
        log.info("sessione chiusa   utente={} aperte={}", nome(evento.getUser()), aperte.decrementAndGet());
    }

    private String nome(Principal utente) {
        return utente == null ? "ANONIMO" : utente.getName();
    }
}
